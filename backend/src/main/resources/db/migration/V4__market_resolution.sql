ALTER TABLE markets ADD COLUMN winning_outcome_id BIGINT REFERENCES market_outcomes (id);
