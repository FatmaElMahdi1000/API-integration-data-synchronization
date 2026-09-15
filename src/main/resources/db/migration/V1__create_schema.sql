CREATE TABLE Internal_Customer
(
    Customer_Id RAW(16) NOT NULL,
    Customer_name VARCHAR2(80) NOT NULL,

    CONSTRAINT customerIdPK PRIMARY KEY (Customer_Id)
);


CREATE TABLE InternalROLE
(
    role_Id RAW(16) NOT NULL,
    name VARCHAR2(80) NOT NULL UNIQUE, -- From External API

    CONSTRAINT roleIdPK PRIMARY KEY (role_Id)
);


CREATE TABLE Internal_STATUS
(
    Status_Id RAW(16) NOT NULL,
    Status_name VARCHAR2(80) NOT NULL UNIQUE,

    CONSTRAINT pkstatusId PRIMARY KEY (Status_Id)
);


CREATE TABLE Internal_Company
(
    Company_id RAW(16) NOT NULL,
    Customer_Id RAW(16) NOT NULL,

    Company_name VARCHAR2(80) NOT NULL,
    industry VARCHAR2(100),
    website VARCHAR2(255),
    employees NUMBER(11,0),

    CONSTRAINT InternalCompanyPK PRIMARY KEY (Company_id),

    CONSTRAINT customerIdFK1
        FOREIGN KEY (Customer_Id)
            REFERENCES Internal_Customer(Customer_Id)
);


CREATE TABLE InternalUSER
(
    user_id RAW(16) NOT NULL,
    Customer_Id RAW(16) NOT NULL,
    Company_Id RAW(16) NOT NULL,
    role_Id RAW(16) NOT NULL,
    status_Id RAW(16) NOT NULL,

    EXTERNAL_USER_ID VARCHAR2(100) NOT NULL,

    name VARCHAR2(80) NOT NULL,
    email VARCHAR2(100) NOT NULL,
    phone VARCHAR2(100),

    created_At TIMESTAMP WITH TIME ZONE,
    updated_At TIMESTAMP WITH TIME ZONE,

    active NUMBER(1) DEFAULT 1 NOT NULL,

    CONSTRAINT user_customer_external
        UNIQUE (Customer_Id, EXTERNAL_USER_ID),

    CONSTRAINT InternalUSERPK
        PRIMARY KEY (user_id),

    CONSTRAINT customerIdFK2
        FOREIGN KEY (Customer_Id)
            REFERENCES Internal_Customer(Customer_Id),

    CONSTRAINT companyIdFK
        FOREIGN KEY (Company_Id)
            REFERENCES Internal_Company(Company_Id),

    CONSTRAINT roleIdFK
        FOREIGN KEY (role_Id)
            REFERENCES InternalROLE(role_Id),

    CONSTRAINT statusIdFK
        FOREIGN KEY (Status_Id)
            REFERENCES Internal_STATUS(Status_Id)
);
