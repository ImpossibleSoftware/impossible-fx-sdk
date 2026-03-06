from __future__ import annotations

from typing import Any, Optional

import httpx

from .types import (
    BatchInfo,
    BatchResult,
    BatchStatus,
    BatchTask,
    Progress,
    RenderResult,
    TokenResult,
)


class ImpossibleFX:
    """Client for the Impossible FX render API.

    Args:
        region: AWS region for the render endpoint (e.g. "us-east-1").
        api_key: Optional API key for authenticated requests.
        timeout: Request timeout in seconds. Defaults to 120.
    """

    def __init__(
        self,
        region: str = "us-east-1",
        api_key: Optional[str] = None,
        timeout: float = 120.0,
    ) -> None:
        self.region = region
        self.base_url = f"https://render-{region}.impossible.io/v2"

        headers: dict[str, str] = {}
        if api_key is not None:
            headers["Authorization"] = f"Bearer {api_key}"

        self._client = httpx.Client(
            base_url=self.base_url,
            headers=headers,
            timeout=timeout,
        )

    # -- lifecycle --

    def close(self) -> None:
        """Close the underlying HTTP client."""
        self._client.close()

    def __enter__(self) -> ImpossibleFX:
        return self

    def __exit__(self, *args: Any) -> None:
        self.close()

    # -- helpers --

    def _request(
        self,
        method: str,
        path: str,
        **kwargs: Any,
    ) -> Any:
        response = self._client.request(method, path, **kwargs)
        response.raise_for_status()
        if response.headers.get("content-type", "").startswith("application/json"):
            return response.json()
        return None

    # -- core render --

    def render(
        self,
        project_id: str,
        movie: str,
        params: dict[str, Any],
        format: str = "mp4",
        parallel: int = 1,
        routing_key: str = "default",
    ) -> RenderResult:
        """Render a movie and return the result with a download URL.

        Args:
            project_id: The project identifier.
            movie: The movie name or path within the project.
            params: Render parameters (template variables, etc.).
            format: Output format (e.g. "mp4", "gif", "png"). Defaults to "mp4".
            parallel: Number of parallel render workers. Defaults to 1.
            routing_key: Routing key for the render queue. Defaults to "default".

        Returns:
            A RenderResult containing the token, download URL, expiry, and status.
        """
        data = self._request(
            "POST",
            f"/render/{project_id}",
            json={
                "movie": movie,
                "params": params,
                "format": format,
                "parallel": parallel,
                "routingKey": routing_key,
            },
        )
        return RenderResult(
            token=data["token"],
            url=data["url"],
            expires=data["expires"],
            status=data["status"],
        )

    def create_token(
        self,
        project_id: str,
        movie: str,
        params: dict[str, Any],
        routing_key: str = "default",
    ) -> TokenResult:
        """Create a render token without waiting for the render to complete.

        Args:
            project_id: The project identifier.
            movie: The movie name or path within the project.
            params: Render parameters.
            routing_key: Routing key for the render queue. Defaults to "default".

        Returns:
            A TokenResult containing the token, expiry, and status.
        """
        data = self._request(
            "POST",
            f"/token/{project_id}",
            json={
                "movie": movie,
                "params": params,
                "routingKey": routing_key,
            },
        )
        return TokenResult(
            token=data["token"],
            expires=data["expires"],
            status=data["status"],
        )

    def get_url(self, token: str, format: str = "mp4") -> str:
        """Build the download URL for a render token.

        Args:
            token: The render token.
            format: Output format. Defaults to "mp4".

        Returns:
            The full download URL.
        """
        return f"{self.base_url}/get/{token}.{format}"

    def get_progress(self, token: str) -> Progress:
        """Get the rendering progress for a token.

        Args:
            token: The render token.

        Returns:
            A Progress object with done/total frame counts.
        """
        data = self._request("GET", f"/progress/{token}")
        return Progress(
            token=data["token"],
            done=data["done"],
            total=data["total"],
        )

    # -- batch --

    def create_batch(self, routing: str = "default") -> BatchInfo:
        """Create a new batch for grouping render tasks.

        Args:
            routing: Routing key for the batch. Defaults to "default".

        Returns:
            A BatchInfo containing the batch ID.
        """
        data = self._request(
            "POST",
            "/batch",
            json={"routing": routing},
        )
        return BatchInfo(id=data["id"])

    def add_batch_tasks(self, batch_id: str, tasks: list[BatchTask]) -> None:
        """Add render tasks to an existing batch.

        Args:
            batch_id: The batch identifier.
            tasks: List of BatchTask objects to add.
        """
        self._request(
            "POST",
            f"/batch/{batch_id}/tasks",
            json={
                "tasks": [
                    {
                        "movie": task.movie,
                        "params": task.params,
                        "format": task.format,
                    }
                    for task in tasks
                ]
            },
        )

    def run_batch(self, batch_id: str) -> None:
        """Start execution of a batch.

        Args:
            batch_id: The batch identifier.
        """
        self._request("POST", f"/batch/{batch_id}/run")

    def get_batch_status(self, batch_id: str) -> BatchStatus:
        """Get the current status of a batch.

        Args:
            batch_id: The batch identifier.

        Returns:
            A BatchStatus with the batch ID, status string, and task count.
        """
        data = self._request("GET", f"/batch/{batch_id}")
        return BatchStatus(
            id=data["id"],
            status=data["status"],
            tasks=data["tasks"],
        )

    def get_batch_results(self, run_id: str) -> list[BatchResult]:
        """Retrieve results for a completed batch run.

        Args:
            run_id: The batch run identifier.

        Returns:
            A list of BatchResult objects, one per task.
        """
        data = self._request("GET", f"/batch/{run_id}/results")
        return [
            BatchResult(
                token=item["token"],
                url=item["url"],
                status=item["status"],
            )
            for item in data
        ]

    def cancel_batch(self, batch_id: str) -> None:
        """Cancel a running batch.

        Args:
            batch_id: The batch identifier.
        """
        self._request("POST", f"/batch/{batch_id}/cancel")
