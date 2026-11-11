#!/usr/bin/env python3
"""Build the cumulative, source-separated changelog in the latest YANIME release."""

from __future__ import annotations

import json
import os
import re
import sys
from datetime import datetime, timezone
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

API_ROOT = "https://api.github.com"
ORIGINAL_REPOSITORY = "Helandy/YummyTV"
BASELINE_VERSION = "1.28.2"
FORK_REPOSITORY = os.environ.get("GITHUB_REPOSITORY", "MraK0ff/YANIME")
YANIME_VERSION = os.environ.get("YANIME_VERSION", "").strip()
TOKEN = os.environ.get("GH_TOKEN", "").strip()

GENERATED_START = "<!-- YANIME-AUTO-CHANGELOG:START -->"
GENERATED_END = "<!-- YANIME-AUTO-CHANGELOG:END -->"
HIDDEN_TAGS = {"INTERNAL", "CORE", "CHORE", "DOCS"}
HIDDEN_COMMIT_SUBJECTS = {
    "автоматически обновлять историю изменений",
    "переключить автообновление на релизы yanime",
    "переключить автообновления на релизы yanime",
    "разделить ссылки оригинального проекта и yanime",
    "обновить название, иконку и оформление yanime",
}
TAG_PATTERN = re.compile(r"^\[([A-Z][A-Z0-9_-]*)\]", re.IGNORECASE)
LIST_PATTERN = re.compile(r"^(\s*)(?:[-*+]|\d+[.)])\s+")
VERSION_TAG = re.compile(r"^v?\d+(?:\.\d+){1,3}$")
PAGE_SIZE = 100
MAX_RELEASE_BODY_LENGTH = 110_000


def request_json(url: str, method: str = "GET", payload: dict | None = None):
    headers = {
        "Accept": "application/vnd.github+json",
        "User-Agent": "YANIME-release-history",
        "X-GitHub-Api-Version": "2022-11-28",
    }
    if TOKEN:
        headers["Authorization"] = f"Bearer {TOKEN}"
    data = json.dumps(payload).encode("utf-8") if payload is not None else None
    if data is not None:
        headers["Content-Type"] = "application/json"
    request = Request(url, data=data, headers=headers, method=method)
    try:
        with urlopen(request, timeout=45) as response:
            raw = response.read()
            return json.loads(raw.decode("utf-8")) if raw else None
    except HTTPError as error:
        detail = error.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"GitHub API {method} {url} failed: HTTP {error.code}: {detail}") from error
    except URLError as error:
        raise RuntimeError(f"GitHub API request failed: {error}") from error


def list_all(repository: str, endpoint: str, params: dict | None = None) -> list[dict]:
    result: list[dict] = []
    page = 1
    while True:
        query = dict(params or {})
        query.update({"per_page": PAGE_SIZE, "page": page})
        url = f"{API_ROOT}/repos/{repository}/{endpoint}?{urlencode(query)}"
        batch = request_json(url)
        if not isinstance(batch, list):
            raise RuntimeError(f"Expected a list from {url}")
        result.extend(batch)
        if len(batch) < PAGE_SIZE:
            return result
        page += 1


def parse_date(value: str | None) -> datetime | None:
    if not value:
        return None
    return datetime.fromisoformat(value.replace("Z", "+00:00")).astimezone(timezone.utc)


def format_date(value: str | None) -> str:
    parsed = parse_date(value)
    return parsed.strftime("%d.%m.%Y") if parsed else "дата неизвестна"


def visible_commit(commit: dict) -> bool:
    message = (commit.get("commit", {}).get("message") or "").strip()
    subject = message.splitlines()[0].strip() if message else ""
    if not subject or subject.lower().startswith(("merge ", "merge pull request")):
        return False
    tag = TAG_PATTERN.match(subject)
    if tag and tag.group(1).upper() in HIDDEN_TAGS:
        return False
    normalized_subject = TAG_PATTERN.sub("", subject, count=1).casefold().strip()
    return not any(
        normalized_subject == hidden or normalized_subject.startswith(hidden + " ")
        for hidden in HIDDEN_COMMIT_SUBJECTS
    )


def commit_date(commit: dict) -> datetime:
    metadata = commit.get("commit", {})
    value = (metadata.get("committer") or {}).get("date") or (metadata.get("author") or {}).get("date")
    return parse_date(value) or datetime.min.replace(tzinfo=timezone.utc)


def release_tag_is_stable(release: dict) -> bool:
    tag = release.get("tag_name", "")
    return (
        not release.get("draft", False)
        and not release.get("prerelease", False)
        and VERSION_TAG.fullmatch(tag) is not None
        and parse_date(release.get("published_at")) is not None
    )


def stable_releases(repository: str) -> list[dict]:
    releases = [
        release
        for release in list_all(repository, "releases")
        if release_tag_is_stable(release)
    ]
    return sorted(releases, key=lambda item: parse_date(item["published_at"]), reverse=True)


def fetch_commits(repository: str, since: datetime) -> list[dict]:
    since_text = since.astimezone(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z")
    commits = list_all(repository, "commits", {"sha": "main", "since": since_text})
    return [commit for commit in commits if visible_commit(commit)]


def release_version(release: dict) -> str:
    return release.get("tag_name", "").removeprefix("v")


def strip_generated_block(body: str) -> str:
    pattern = re.compile(
        re.escape(GENERATED_START) + r".*?" + re.escape(GENERATED_END),
        re.DOTALL,
    )
    return pattern.sub("", body or "").strip()


def without_hidden_release_entries(body: str) -> str:
    if not body.strip():
        return ""
    visible: list[str] = []
    hidden_indent: int | None = None
    for line in body.splitlines():
        trimmed = line.lstrip()
        is_heading = trimmed.startswith("#")
        list_match = LIST_PATTERN.match(line)
        if is_heading:
            content = trimmed.lstrip("#").strip()
        elif list_match:
            content = line[list_match.end():].strip()
        else:
            content = trimmed
        tag_match = TAG_PATTERN.match(content)
        tag = tag_match.group(1).upper() if tag_match else None

        if is_heading:
            hidden_indent = -1 if tag in HIDDEN_TAGS else None
            if tag not in HIDDEN_TAGS:
                visible.append(line)
            continue
        if list_match:
            indent = len(list_match.group(1))
            if hidden_indent is not None and indent <= hidden_indent:
                hidden_indent = None
            if tag in HIDDEN_TAGS:
                hidden_indent = indent
                continue
            if hidden_indent is None:
                visible.append(line)
            continue
        if tag:
            if tag in HIDDEN_TAGS:
                hidden_indent = -1
                continue
            hidden_indent = None
            visible.append(line)
            continue
        if hidden_indent is None:
            visible.append(line)
    return "\n".join(visible).strip()


def commit_link(commit: dict) -> str:
    message = (commit.get("commit", {}).get("message") or "").strip()
    subject = message.splitlines()[0].strip()
    tag = TAG_PATTERN.match(subject)
    if tag:
        subject = f"{tag.group(1).upper()} · {subject[tag.end():].strip()}"
    subject = subject.replace("[", "(").replace("]", ")")
    url = commit.get("html_url") or f"https://github.com/{FORK_REPOSITORY}/commit/{commit['sha']}"
    return f"- [{subject}]({url})"


def commits_for_release(
    releases: list[dict],
    index: int,
    commits: list[dict],
    oldest_history_start: datetime | None = None,
) -> list[dict]:
    release_date = parse_date(releases[index]["published_at"])
    if release_date is None:
        return []
    lower_bound = release_date
    if index == len(releases) - 1 and oldest_history_start is not None:
        lower_bound = min(lower_bound, oldest_history_start)
    upper_bound = (
        parse_date(releases[index - 1]["published_at"])
        if index > 0
        else None
    )
    selected = [
        commit for commit in commits
        if commit_date(commit) >= lower_bound
        and (upper_bound is None or commit_date(commit) < upper_bound)
    ]
    return sorted(selected, key=commit_date, reverse=True)


def strip_issue_links(body: str) -> str:
    """Keep issue-link labels in upstream notes, but remove external issue links."""
    body = re.sub(
        r"\[([^\]]+)\]\(https?://github\.com/[^/]+/[^/]+/issues/[^)]+\)",
        r"\1",
        body,
        flags=re.IGNORECASE,
    )
    return re.sub(
        r"https?://github\.com/[^/]+/[^/]+/issues/\d+",
        "",
        body,
        flags=re.IGNORECASE,
    ).strip()


def card_block(title: str, date: str, version: str, body: str) -> str:
    metadata = "|".join((title.replace("|", " "), date, version))
    return f"<!-- YANIME-CARD|{metadata} -->\n{body.strip()}\n<!-- /YANIME-CARD -->"


def load_manual_yanime_releases() -> list[dict]:
    path = Path(__file__).resolve().parents[2] / "docs/changelog/yanime.json"
    if not path.is_file():
        raise RuntimeError(f"Manual YANIME changelog is missing: {path}")
    data = json.loads(path.read_text(encoding="utf-8"))
    releases = data.get("releases")
    if not isinstance(releases, list):
        raise RuntimeError("docs/changelog/yanime.json must contain a releases array")

    seen_versions: set[str] = set()
    seen_commits: set[str] = set()
    for release in releases:
        version = str(release.get("version", "")).strip()
        notes = release.get("notes")
        overrides = release.get("commit_overrides", [])
        if not version or version in seen_versions:
            raise RuntimeError(f"Invalid or duplicate YANIME changelog version: {version!r}")
        if not isinstance(notes, list) or not notes or not all(str(note).strip() for note in notes):
            raise RuntimeError(f"YANIME {version} must have a non-empty notes array")
        if not isinstance(overrides, list):
            raise RuntimeError(f"YANIME {version}: commit_overrides must be an array")
        for override in overrides:
            sha = str(override.get("sha", "")).strip().lower()
            title = str(override.get("title", "")).strip()
            if not re.fullmatch(r"[0-9a-f]{40}", sha):
                raise RuntimeError(f"YANIME {version}: invalid commit SHA in override: {sha!r}")
            if sha in seen_commits:
                raise RuntimeError(f"Commit override is duplicated: {sha}")
            if not title:
                raise RuntimeError(f"YANIME {version}: commit override {sha} has no title")
            seen_commits.add(sha)
        seen_versions.add(version)
    return releases


def commit_overrides_for(releases: list[dict]) -> dict[str, dict]:
    return {
        str(override["sha"]).lower(): override
        for release in releases
        for override in release.get("commit_overrides", [])
    }


def format_curated_override(override: dict, original_tag: str = "") -> str:
    tag = original_tag or str(override.get("tag", "")).strip()
    tag = f"[{tag.strip('[]')}] " if tag else ""
    title = str(override["title"]).strip().rstrip(".")
    result = f"- {tag}{title}"
    description = str(override.get("description", "")).strip()
    if description:
        result += "\n  " + description.replace("\n", "\n  ")
    return result


def render_yanime_release_card(release: dict) -> str:
    entries = [f"- {str(note).strip()}" for note in release["notes"]]
    entries.extend(format_curated_override(item) for item in release.get("commit_overrides", []))
    return card_block("YANIME", format_date(release.get("published_at")), str(release["version"]), "\n".join(entries))


def commits_for_release(
    releases: list[dict],
    index: int,
    commits: list[dict],
    oldest_history_start: datetime | None = None,
) -> list[dict]:
    release_date = parse_date(releases[index]["published_at"])
    if release_date is None:
        return []
    lower_bound = release_date
    if index == len(releases) - 1 and oldest_history_start is not None:
        lower_bound = min(lower_bound, oldest_history_start)
    upper_bound = parse_date(releases[index - 1]["published_at"]) if index > 0 else None
    selected = [
        commit for commit in commits
        if commit_date(commit) >= lower_bound
        and (upper_bound is None or commit_date(commit) < upper_bound)
    ]
    return sorted(selected, key=commit_date, reverse=True)


def commit_bullets(commits: list[dict], overrides: dict[str, dict] | None = None) -> list[str]:
    bullets: list[str] = []
    overrides = overrides or {}
    for commit in commits:
        message = (commit.get("commit", {}).get("message") or "").strip()
        if not message:
            continue
        lines = message.splitlines()
        subject = lines[0].strip()
        tag_match = TAG_PATTERN.match(subject)
        original_tag = tag_match.group(0) if tag_match else ""
        override = overrides.get(str(commit.get("sha", "")).lower())
        if override:
            title = str(override["title"]).strip().rstrip(".")
            tag = original_tag or (
                f"[{str(override.get('tag', '')).strip('[]')}] "
                if override.get("tag") else ""
            )
            description = str(override.get("description", "")).strip()
        else:
            title = TAG_PATTERN.sub("", subject, count=1).strip().rstrip(".")
            tag = original_tag
            description = "\n".join(lines[1:]).strip()

        if not title:
            continue
        bullet = f"- {tag + ' ' if tag and not tag.endswith(' ') else tag}{title}"
        if description:
            bullet += "\n  " + description.replace("\n", "\n  ")
        bullets.append(bullet)
    return bullets


def render_yanime_post_release_card(
    releases: list[dict],
    commits: list[dict],
    baseline_date: datetime,
    overrides: dict[str, dict],
) -> list[str]:
    if not releases:
        return []
    changes = commits_for_release(releases, 0, commits, oldest_history_start=baseline_date)
    # Overrides already appear in the curated release card; don't show the same
    # change a second time in the automatically collected post-release list.
    changes = [item for item in changes if str(item.get("sha", "")).lower() not in overrides]
    if not changes:
        return []
    version = release_version(releases[0])
    return [card_block(f"Изменения после версии {version}", "", "YANIME", "\n".join(commit_bullets(changes)))]


def remove_leading_heading(body: str) -> str:
    lines = body.splitlines()
    while lines and not lines[0].strip():
        lines.pop(0)
    if lines and lines[0].lstrip().startswith("#"):
        lines.pop(0)
    return "\n".join(lines).strip()


def render_latest_original_release_card(
    releases: list[dict],
    commits: list[dict],
    baseline_date: datetime,
) -> str:
    if not releases:
        raise RuntimeError("No stable YummyTV release found at or after the history baseline")
    release = releases[0]
    body = remove_leading_heading(
        strip_issue_links(
            without_hidden_release_entries(strip_generated_block(release.get("body") or ""))
        )
    )
    changes = commits_for_release(releases, 0, commits, oldest_history_start=baseline_date)
    bullets = commit_bullets(changes)
    if bullets:
        body = "\n\n".join(part for part in (body, "\n".join(bullets)) if part)
    if not body:
        raise RuntimeError(f"YummyTV {release_version(release)} has no visible release notes")
    return card_block("YummyTV", format_date(release.get("published_at")), release_version(release), body)


def patch_release(release: dict, body: str) -> None:
    request_json(
        f"{API_ROOT}/repos/{FORK_REPOSITORY}/releases/{release['id']}",
        method="PATCH",
        payload={"body": body},
    )


def main() -> int:
    if not TOKEN:
        raise RuntimeError("GH_TOKEN is required")
    if not YANIME_VERSION:
        raise RuntimeError("YANIME_VERSION is required")

    manual_releases = load_manual_yanime_releases()
    manual_release = next((item for item in manual_releases if item["version"] == YANIME_VERSION), None)
    if manual_release is None:
        raise RuntimeError(
            f"YANIME {YANIME_VERSION} is missing from docs/changelog/yanime.json; "
            "add its curated notes before publishing"
        )

    all_original_releases = stable_releases(ORIGINAL_REPOSITORY)
    baseline_tag = f"v{BASELINE_VERSION}"
    baseline_release = next(
        (release for release in all_original_releases if release.get("tag_name") == baseline_tag),
        None,
    )
    if baseline_release is None:
        raise RuntimeError(f"Original baseline release {baseline_tag} was not found")

    baseline_date = parse_date(baseline_release.get("published_at"))
    if baseline_date is None:
        raise RuntimeError(f"Original baseline release {baseline_tag} has no publication date")

    original_releases = [
        release for release in all_original_releases
        if parse_date(release.get("published_at")) >= baseline_date
    ]
    own_releases = [
        release for release in stable_releases(FORK_REPOSITORY)
        if parse_date(release.get("published_at")) >= baseline_date
    ]
    if not own_releases:
        raise RuntimeError(f"No stable YANIME release found since {BASELINE_VERSION}")

    target_tag = f"v{YANIME_VERSION}"
    latest_release = next(
        (release for release in own_releases if release.get("tag_name") == target_tag),
        own_releases[0],
    )

    original_commits = fetch_commits(ORIGINAL_REPOSITORY, baseline_date)
    original_shas = {commit.get("sha") for commit in original_commits}
    fork_commits_all = fetch_commits(FORK_REPOSITORY, baseline_date)

    fork_commits: list[dict] = []
    for commit in fork_commits_all:
        author = commit.get("author") or {}
        committer = commit.get("committer") or {}
        if commit.get("sha") in original_shas:
            continue
        if (author.get("login") or "").casefold() == "helandy":
            continue
        if (committer.get("login") or "").casefold() == "helandy":
            continue
        fork_commits.append(commit)

    manual_overrides = commit_overrides_for(manual_releases)
    generated_cards = [
        render_yanime_release_card(manual_release),
        *render_yanime_post_release_card(own_releases, fork_commits, baseline_date, manual_overrides),
        render_latest_original_release_card(original_releases, original_commits, baseline_date),
    ]
    generated = "\n\n".join(generated_cards)

    base_body = strip_generated_block(latest_release.get("body") or "")
    new_body = (f"{base_body}\n\n" if base_body else "") + f"{GENERATED_START}\n{generated}\n{GENERATED_END}"

    if len(new_body) > MAX_RELEASE_BODY_LENGTH:
        raise RuntimeError(
            f"Generated release notes are too long ({len(new_body)} chars); refusing to truncate the history"
        )

    if new_body != (latest_release.get("body") or ""):
        patch_release(latest_release, new_body)
        print(
            f"Updated {latest_release.get('tag_name')}: "
            f"YANIME curated notes {manual_release['version']}, "
            f"YummyTV release {release_version(original_releases[0])}, "
            f"{len(original_commits)} original commits, {len(fork_commits)} fork commits."
        )
    else:
        print(f"Release {latest_release.get('tag_name')} history is already up to date.")

    for release in own_releases:
        if release.get("id") == latest_release.get("id"):
            continue
        body = release.get("body") or ""
        cleaned = strip_generated_block(body)
        if cleaned != body:
            patch_release(release, cleaned)
            print(f"Removed stale generated history from {release.get('tag_name')}.")

    return 0

if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:
        print(f"::error::{error}", file=sys.stderr)
        raise
