# Digital Heroes implementation assumptions

This implementation follows the supplied Digital Heroes PRD and the previously agreed Phase 2 assumptions.

- A4: the user's latest five Stableford scores are the five draw numbers.
- A5/A13: algorithmic draw weighting uses raw score frequency with Laplace smoothing: frequency + 1.
- A10: monthly draw eligibility requires an ACTIVE subscription and exactly five stored scores.
- A11: simulation snapshots participant scores. Re-running simulation regenerates the snapshot; publishing freezes it.
- A12: duplicate score values are allowed across different dates. Matching uses distinct values; frequency weighting uses raw occurrences.
- A14: prize tiers are exclusive: a 5-match winner is paid only from FIVE, 4-match only FOUR, 3-match only THREE.
- A15: equal winner split uses scale 2 and HALF_UP/DOWN-safe accounting, with residual recorded in `prize_pools.rounding_residual`.
- A16: the FIVE tier uses a persistent `jackpot_ledger`; if there is no five-match winner the total FIVE pool rolls forward.

## Payment note

The backend is wired for Stripe identifiers, payment records, and future webhook integration, but local/demo activation uses a `DEMO` payment provider so the application can be tested without exposing payment credentials. Production deployment should replace that activation flow with a PCI-compliant Stripe Checkout/subscription flow and verified webhooks.

## Proof storage note

Winner proof uploads are restricted to the winner and currently stored in a private server-side temporary directory for local/demo operation. Production deployment should use a private Supabase Storage bucket with signed URLs.

## Deployment

- Backend: Spring Boot 3.3.4, Java 17, PostgreSQL/Supabase, Flyway.
- Frontend: React + Vite under `frontend/`.
- Backend base URL is configured through `VITE_API_URL`.
- Never commit `.env` or real database/JWT/Stripe credentials.
