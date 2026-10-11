## Running the app

### Prerequisites
- Docker Desktop
- A Supabase project (Postgres with the pgvector extension, enabled automatically on first start)
- An AWS S3 bucket and access keys
- API keys from Anthropic (console.anthropic.com) and Voyage AI (dashboard.voyageai.com; add a payment method to avoid trial rate limits)

### Setup
1. Clone the repo.
2. Copy `.env.example` to `.env` and fill in every value.
3. From the repo root, run `docker compose up --build`.
4. Open http://localhost:5173

The database tables are created automatically on first start.