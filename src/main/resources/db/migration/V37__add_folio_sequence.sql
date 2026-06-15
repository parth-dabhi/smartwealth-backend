-- V37__add_folio_sequence.sql
CREATE SEQUENCE folio_number_seq
    START WITH 10000000
    INCREMENT BY 1
    MINVALUE 10000000
    NO MAXVALUE
    CACHE 1;