-- Add holding_id to investment_order and sip_mandate tables to reference user_holdings

ALTER TABLE investment_order
    ADD COLUMN holding_id BIGINT;

ALTER TABLE investment_order
    ADD CONSTRAINT fk_investment_order_holding
        FOREIGN KEY (holding_id)
            REFERENCES user_holdings (holding_id)
            ON DELETE RESTRICT;

-------------------------------------------------------

-- SIP mandates should also reference holding_id
ALTER TABLE sip_mandate
    ADD COLUMN holding_id BIGINT;

ALTER TABLE sip_mandate
    ADD CONSTRAINT fk_sip_mandate_holding
        FOREIGN KEY (holding_id)
            REFERENCES user_holdings (holding_id)
            ON DELETE RESTRICT;