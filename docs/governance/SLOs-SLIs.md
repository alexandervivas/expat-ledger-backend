# SLOs & SLIs

## Objectives

- API P95 latency < 200 ms (steady-state, **excluding first-hit-after-idle on Render Free**)
- Error rate < 0.1%
- MTTR < 30 min

## Indicators (examples)

- Request latency histogram → P95 (PromQL)
- Error ratio of 5xx over total
- DLQ depth for messaging (later when broker is hosted)
- **First-hit-after-idle P95** (informational metric, not part of SLO)

## PromQL sketches

- **API P95**:
  ```
  histogram_quantile(0.95, sum(rate(http_server_request_duration_seconds_bucket{service="api"}[5m])) by (le))
  ```
- **Error rate**:
  ```
  sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))
  ```
- **Cold-start / first-hit-after-idle** (example patterns):
  - Option A: expose a dedicated histogram `app_cold_start_request_seconds_bucket` with label `reason="idle"` and chart P95.
  - Option B: add a log/metric label `is_cold_start="true"` on the first request after idle and compute its P95 separately.

## Notes

- Render Free may **idle/suspend** services. We explicitly **exclude** these cold-start latencies from the steady-state latency SLO, but we **track and visualize** them to set user expectations during demos.
- Document demo windows if using optional keep-alive pings; do **not** run persistent keep-alive.
