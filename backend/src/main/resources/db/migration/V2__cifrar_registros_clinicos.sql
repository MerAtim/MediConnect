-- Los registros existentes se guardaron en texto plano, antes de que
-- existiera AesGcmFieldEncryptor -- no hay forma de desencriptarlos bajo
-- el nuevo esquema (no se cifraron nunca), así que se truncan como parte
-- del corte a cifrado at-rest. En este proyecto son solo datos de prueba
-- locales (nunca hubo contenido médico real); en un despliegue con datos
-- reales, este TRUNCATE se reemplazaría por un script de migración que
-- lee cada fila en texto plano vía JPA (antes del deploy con el
-- converter activo) y la reescribe cifrada, en vez de perderla.
TRUNCATE TABLE registros_clinicos;

ALTER TABLE registros_clinicos
    ALTER COLUMN diagnostico TYPE TEXT,
    ALTER COLUMN tratamiento TYPE TEXT,
    ALTER COLUMN observaciones TYPE TEXT;
