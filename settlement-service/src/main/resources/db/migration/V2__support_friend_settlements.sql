-- Allow settlements without a group (direct friend-to-friend settlements)
ALTER TABLE settlements ALTER COLUMN group_id DROP NOT NULL;
