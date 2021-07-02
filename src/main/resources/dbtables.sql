CREATE TABLE IF NOT EXISTS `collectors_collectors` (
    `collector_id` BIGINT NOT NULL,
    `mode` VARCHAR(9) NOT NULL DEFAULT 'ALL',
    `location` VARCHAR(255) NOT NULL,
    CONSTRAINT `collectors_collectors_id_pk` PRIMARY KEY (`collector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `collectors_contents` (
    `collector_id` BIGINT NOT NULL,
    `material` VARCHAR(255) NOT NULL,
    `amount` INT NOT NULL,
    CONSTRAINT `collectors_contents_ck` PRIMARY KEY (`collector_id`, `material`),
    CONSTRAINT `collectors_contents_id_fk`
        FOREIGN KEY (`collector_id`) REFERENCES `collectors_collectors` (`collector_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;