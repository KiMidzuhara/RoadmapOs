create table roadmaps (
    id BIGINT generated always as identity primary key,
    title VARCHAR(255) not null,
    description TEXT
);

create table nodes (
    id BIGINT generated always as identity primary key,
    roadmap_id BIGINT not null references roadmaps(id) on delete cascade,
    title VARCHAR(255) not null,
    position_x double precision not null default 0.0,
    position_y double precision not null default 0.0,
    status VARCHAR(50) default 'LOCKED'
);

CREATE TABLE edges (
    id BIGINT generated always as identity primary key,
    roadmap_id BIGINT NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    source_node_id BIGINT NOT NULL REFERENCES nodes(id) ON DELETE CASCADE,
    target_node_id BIGINT NOT NULL REFERENCES nodes(id) ON DELETE CASCADE,
    -- нельзя создать две одинаковые связи
    CONSTRAINT uq_edges_source_target UNIQUE (source_node_id, target_node_id)
);

CREATE INDEX idx_edges_roadmap_id ON edges (roadmap_id);