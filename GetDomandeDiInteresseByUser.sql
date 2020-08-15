DELIMITER $$
CREATE PROCEDURE GetDomandeDiInteresseByUser(_idUtente VARCHAR(256))
BEGIN
	SELECT *
    FROM Domande AS Dom
    INNER JOIN CategorieDomande AS CaDom
		ON(Dom.idDomanda=CaDom.idDomanda)
	INNER JOIN Interessi AS Inter
		ON(CaDom.idCategoria=Inter.idCategoria)
	WHERE Inter.idUtente=_idUtente
    ORDER BY Dom.dataPubblicazione DESC;
END;
DELIMITER;
