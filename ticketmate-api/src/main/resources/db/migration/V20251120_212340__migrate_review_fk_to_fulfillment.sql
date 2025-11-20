-- 성공하지 않은 티켓팅에 대한 리뷰가 존재하는지 검사
DO
$$
    DECLARE
    v_count BIGINT;
    BEGIN
        SELECT COUNT(*)
        INTO v_count
        FROM public.review r
                 LEFT JOIN public.fulfillment_form f
                           ON r.application_form_application_form_id = f.application_form_application_form_id
        WHERE f.fulfillment_form_id IS NULL;

        IF v_count > 0 THEN
                RAISE EXCEPTION
                    'Flyway 마이그레이션 에러: fulfillment_form과 매핑되지 않은 review가 %개 존재합니다.',
                    v_count;
        END IF;
    END;
$$;

-- application_form_id FK 제거
ALTER TABLE public.review
DROP CONSTRAINT IF EXISTS review_application_form_application_form_id_fkey;

-- application_form_id UNIQUE 제약 조건 제거
ALTER TABLE public.review
DROP CONSTRAINT IF EXISTS review_application_form_application_form_id_key;

-- 컬럼 추가
ALTER TABLE public.review
    ADD COLUMN fulfillment_form_fulfillment_form_id UUID;

-- 성공한 리뷰 매핑
UPDATE review r
SET fulfillment_form_fulfillment_form_id = f.fulfillment_form_id
    FROM fulfillment_form f
WHERE r.application_form_application_form_id = f.application_form_application_form_id;

-- fulfillment_form_id NOT NULL 적용
ALTER TABLE review
    ALTER COLUMN fulfillment_form_fulfillment_form_id SET NOT NULL;

-- fulfillment_form_id UNIQUE 제약 추가
ALTER TABLE review
    ADD CONSTRAINT review_fulfillment_form_fulfillment_form_id_fkey UNIQUE (fulfillment_form_fulfillment_form_id);

-- application_form_id 컬럼 삭제
ALTER TABLE public.review
DROP COLUMN IF EXISTS application_form_application_form_id;