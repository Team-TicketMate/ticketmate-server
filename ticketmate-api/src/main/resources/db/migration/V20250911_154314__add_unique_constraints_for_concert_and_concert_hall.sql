-- Concert & ConcertHall 엔티티 변경
-- 1. Concert.concertName unique 제약조건 추가
-- 2. ConcertHall.concertHallName unique 제약조건 추가
-- 3. ConcertHall.webSiteUrl unique 제약조건 추가

-- 사전 중복 검사 (중복 있는 경우 에러로 중단)
DO
$$
  BEGIN
    -- concert_hall_name 중복 검사
    IF EXISTS(
      SELECT concert_hall_name
      FROM concert_hall
      GROUP BY concert_hall_name
      HAVING COUNT(*) > 1
    ) THEN
      RAISE EXCEPTION '공연장명 unique 제약조건을 추가할 수 없습니다: 중복된 공연장명이 이미 존재합니다.';
    END IF;

    -- web_site_url 중복 검사 (null 제외)
    IF EXISTS(
      SELECT web_site_url
      FROM concert_hall
      WHERE web_site_url IS NOT NULL
      GROUP BY web_site_url
      HAVING COUNT(*) > 1
    ) THEN
      RAISE EXCEPTION '웹사이트 URL unique 제약조건을 추가할 수 없습니다: 중복된 웹사이트 URL이 이미 존재합니다 (null 허용)';
    END IF;

    -- concert_name 중복 검사
    IF EXISTS(
      SELECT concert_name
      FROM concert
      GROUP BY concert_name
      HAVING COUNT(*) > 1
    ) THEN
      RAISE EXCEPTION '공연명 unique 제약조건을 추가할 수 없습니다: 중복된 공연명이 이미 존재합니다';
    END IF;
  END
$$;

-- 1) concert_hall.concert_hall_name UNIQUE 제약 추가 (존재하지 않을 때만)
DO
$$
  BEGIN
    IF NOT EXISTS (
      SELECT 1
      FROM pg_constraint
      WHERE conname = 'uq_concert_hall_concert_hall_name'
    ) THEN
      ALTER TABLE concert_hall
        ADD CONSTRAINT uq_concert_hall_concert_hall_name
          UNIQUE (concert_hall_name);
    END IF;
  END
$$;

-- 2) concert_hall.web_site_url UNIQUE 제약 추가 (존재하지 않을 때만)
DO
$$
  BEGIN
    IF NOT EXISTS (
      SELECT 1
      FROM pg_constraint
      WHERE conname = 'uq_concert_hall_web_site_url'
    ) THEN
      ALTER TABLE concert_hall
        ADD CONSTRAINT uq_concert_hall_web_site_url
          UNIQUE (web_site_url);
    END IF;
  END
$$;

-- 3) concert.concert_name UNIQUE 제약 추가 (존재하지 않을 때만)
DO
$$
  BEGIN
    IF NOT EXISTS (
      SELECT 1
      FROM pg_constraint
      WHERE conname = 'uq_concert_concert_name'
    ) THEN
      ALTER TABLE concert
        ADD CONSTRAINT uq_concert_concert_name
          UNIQUE (concert_name);
    END IF;
  END
$$;