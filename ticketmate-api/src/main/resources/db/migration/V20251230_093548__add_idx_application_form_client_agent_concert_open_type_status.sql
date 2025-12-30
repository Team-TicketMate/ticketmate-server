-- application_form
-- client_member_id, agent_member_id, concert_concert_id, ticket_open_type, application_form_status
-- 복합인덱스 추가

CREATE INDEX IF NOT EXISTS idx_application_form_client_agent_concert_open_type_status
  ON application_form (
                       client_member_id,
                       agent_member_id,
                       concert_concert_id,
                       ticket_open_type,
                       application_form_status
    );