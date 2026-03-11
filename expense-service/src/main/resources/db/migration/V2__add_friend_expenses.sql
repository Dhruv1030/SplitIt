-- Support direct friend-to-friend expenses (not tied to a group)
ALTER TABLE expenses ALTER COLUMN group_id DROP NOT NULL;

ALTER TABLE expenses ADD COLUMN friend_user_id VARCHAR(255);

ALTER TABLE expenses ADD COLUMN expense_type VARCHAR(20) NOT NULL DEFAULT 'GROUP';

-- Index for querying friend expenses efficiently
CREATE INDEX idx_expenses_friend_user_id ON expenses(friend_user_id) WHERE friend_user_id IS NOT NULL;
CREATE INDEX idx_expenses_expense_type ON expenses(expense_type);
