-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
	id uuid NOT NULL,
	email varchar(255) NOT NULL,
	username varchar(50) NOT NULL,
    display_name varchar(50) NULL,
	avatar_url varchar(500) NULL,
	last_login timestamp NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT users_pk PRIMARY KEY (id),
	CONSTRAINT users_unique UNIQUE (email),
	CONSTRAINT users_unique_1 UNIQUE (username)
);


-- public.user_identity_providers definition

-- Drop table

-- DROP TABLE public.user_identity_providers;

CREATE TABLE public.user_identity_providers (
	id uuid NOT NULL,
	user_id uuid NOT NULL,
	email varchar(255) NOT NULL,
	provider varchar(20) NOT NULL,
	provider_id varchar(255) NOT NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT user_identity_providers_pk PRIMARY KEY (id),
	CONSTRAINT user_identity_providers_users_fk FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);