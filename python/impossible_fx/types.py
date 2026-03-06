from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Optional


@dataclass
class RenderResult:
    """Result of a render request."""

    token: str
    url: str
    expires: str
    status: str


@dataclass
class TokenResult:
    """Result of a create_token request."""

    token: str
    expires: str
    status: str


@dataclass
class Progress:
    """Progress information for a render."""

    token: str
    done: int
    total: int


@dataclass
class BatchTask:
    """A single task within a batch render."""

    movie: str
    params: dict[str, Any]
    format: str = "mp4"


@dataclass
class BatchInfo:
    """Information about a created batch."""

    id: str


@dataclass
class BatchStatus:
    """Status of a batch run."""

    id: str
    status: str
    tasks: int


@dataclass
class BatchResult:
    """Result of a single task within a completed batch."""

    token: str
    url: str
    status: str
