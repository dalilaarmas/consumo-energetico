CREATE TABLE municipio (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE distribuidor (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE cups (
                      codigo VARCHAR(25) PRIMARY KEY,
                      direccion VARCHAR(255),
                      codigo_postal INT,
                      municipio_id INT NOT NULL,
                      distribuidor_id INT NOT NULL,
                      CONSTRAINT fk_cups_municipio
                          FOREIGN KEY (municipio_id) REFERENCES municipio(id),
                      CONSTRAINT fk_cups_distribuidor
                          FOREIGN KEY (distribuidor_id) REFERENCES distribuidor(id)
);

CREATE TABLE consumo (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         cups_codigo VARCHAR(25) NOT NULL,
                         fecha DATE NOT NULL,
                         consumo DECIMAL(12,2) NOT NULL,
                         CONSTRAINT fk_consumo_cups
                             FOREIGN KEY (cups_codigo) REFERENCES cups(codigo),
                         CONSTRAINT uq_cups_fecha
                             UNIQUE (cups_codigo, fecha)
);

CREATE INDEX idx_consumo_fecha ON consumo(fecha);