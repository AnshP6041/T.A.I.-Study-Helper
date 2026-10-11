create extension if not exists vector;

create table if not exists document_chunks (
                                               id             uuid primary key,
                                               document_id    uuid not null references documents(id) on delete cascade,
    course_id      uuid not null,
    page_number    int  not null,
    chunk_index    int  not null,
    content        text not null,
    page_image_key text,
    embedding      vector(1024) not null,
    content_tsv    tsvector generated always as (to_tsvector('english', content)) stored
    );

create index if not exists document_chunks_embedding_idx on document_chunks using hnsw (embedding vector_cosine_ops);
create index if not exists document_chunks_tsv_idx       on document_chunks using gin (content_tsv);
create index if not exists document_chunks_course_idx    on document_chunks (course_id);

alter table documents drop constraint if exists documents_status_check;

alter table courses enable row level security;
alter table documents enable row level security;
alter table document_chunks enable row level security;