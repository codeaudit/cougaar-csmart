create table V7_TPFDD (
RLN                  VARCHAR2(7),
SERVICE_CODE         CHAR(1),
UIC                  VARCHAR(6),
ULC                  VARCHAR(3),
UTC                  VARCHAR(5),
UNIT_NAME            VARCHAR(50),
DESCRIPTION          VARCHAR(100),
NUM_PAX              NUMBER,
ORIGIN_GEOLOC        CHAR(4),
ORIGIN_NAME          VARCHAR(50),
POE_GEOLOC           CHAR(4),
POE_NAME             VARCHAR(50),
POD_GEOLOC           CHAR(4),
POD_NAME             VARCHAR(50),
DEST_GEOLOC          CHAR(4),
DEST_NAME            VARCHAR(50),
RLD_ORIGIN           NUMBER,
ALD_POE              NUMBER,
EAD_POD              NUMBER,
LAD_POD              NUMBER,
RDD_DEST             NUMBER
)
;
