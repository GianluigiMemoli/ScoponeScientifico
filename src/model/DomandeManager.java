package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.Part;

import Exceptions.ErrorePubblicazioneDomandaException;

public class DomandeManager {
	
	public DomandaBean pubblicaDomanda (
		PartecipanteBean autore,
		String titolo,
		String corpo,
		Date dataPubblicazione,
		String[] idCategorie,
		List<Part> allegati) 
				throws Exception
	{
		int lunghezzaTitolo = titolo.trim().length(); 
		
		logger.info("Lunghezza titolo = " + lunghezzaTitolo);
		
		if(lunghezzaTitolo < 2) {
			throw new ErrorePubblicazioneDomandaException("Il titolo deve contenere almeno due caratteri.");
		}
		
		int lunghezzaCorpo = corpo.trim().length(), numeroAllegati = allegati.size();
		
		logger.info("Lunghezza corpo = " + lunghezzaCorpo);
		logger.info("Numero di allegati = " + numeroAllegati);
		
		if(lunghezzaCorpo == 0 &&  numeroAllegati == 0) {
			throw new ErrorePubblicazioneDomandaException("Il corpo e gli allegati di una domanda non possono essere entrambi nulli.");
		}
		
		int numeroCategorie = idCategorie.length;
		
		logger.info("Numero di categorie = " + numeroCategorie);
		
		if(numeroCategorie == 0) {
			throw new ErrorePubblicazioneDomandaException("Bisogna scegliere almeno un categoria per poter pubblicare una domanda");
		}
		
		//TODO Il controllo su estensione e dimensione degli allegati ci vuole solo per la creazione di certificazioni e quindi non serve per la pubblicazione della domande
		
		// Inserimento domanda all'interno della tabella domande
		
		DomandaBean domanda = new DomandaBean();
		
		domanda.setAutore(autore);
		domanda.setTitolo(titolo);
		domanda.setCorpo(corpo);
		domanda.setDataPubblicazione(dataPubblicazione);
		domanda.setArchiviata(false);
		
		DomandaDAO.addDomanda(domanda);
		
		// Inserimento categorie all'interno della tabella categoriedomande
	
		CategoriaBean categoria = new CategoriaBean();
		
		for (String idCategoria : idCategorie) {		
			System.out.println("INSERISCO IN CATEGORIE DOMANDE ID CATEGORIA " + idCategoria);
			categoria.setId(idCategoria);
			DomandaDAO.addCategoriaDomanda(domanda, categoria);
		}
		
		// Inserimento allegati
		
		AllegatiHandler allegatiHandler = new AllegatiHandler();
		allegatiHandler.caricaAllegati(allegati, UPLOAD_PATH + domanda.getId());
		
		return domanda;
	}
	
	public ArrayList<DomandaBean> ricerca(String testo, String[] categorie, Boolean isArchiviata) throws Exception {
		if(testo != null)
			if(testo.length() < 3)
				throw new Exception("Il testo, se inserito, deve essere di almeno 3 caratteri.");
		
		// TODO Aggiungere l'eliminazione delle congiunzioni, articoli ecc per la ricerca full text
		
		return DomandaDAO.getDomandeCercate(testo, isArchiviata, categorie);	
	}
	
	public DomandaBean getDomandaById(String idDomanda) {
		
		// domanda
		DomandaBean domanda = DomandaDAO.getDomandaById(idDomanda);
		
		if(domanda != null) {
		
			// autore
			String idAutore = domanda.getAutore().getId();
			domanda.setAutore(UtenteDAO.getUtenteById(idAutore));
			
			// categorie
			domanda.setCategorie(CategoriaDAO.getCategorieDomandaByIdDomanda(idDomanda));
		
			// TODO risposte
			ArrayList<RispostaBean> risposte = new ArrayList<RispostaBean>();
			domanda.setRisposte(risposte);
			
			// allegati
			AllegatiHandler allegatiHandler = new AllegatiHandler();
			File[] allegati = allegatiHandler.getAllegati(UPLOAD_PATH + idDomanda);
			
			try {
				domanda.setAllegati(allegatiHandler.convertToBase64(allegati));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
			
		return domanda;
	}
	
	public ArrayList<DomandaBean> getDomandeByAutore(String idAutore) {
		return DomandaDAO.getDomandeByUtente(idAutore);
	}
	
	public ArrayList<DomandaBean> getDomandeByAutore(String idAutore, int start, int end) {
		return DomandaDAO.getDomandeByUtente(idAutore, start, end);
	}
	
	public void removeDomanda(String idDomanda) {
		DomandaDAO.removeDomanda(idDomanda);
	}
	
	public void updateCategorieDomanda(DomandaBean domanda) {
		DomandaDAO.updateCategorieDomanda(domanda);
	}
	
	private DomandaBean populateAutore(DomandaBean domanda) {
		UtenteBean autore = UtenteDAO.getUtenteById(domanda.getAutore().getId());
		domanda.setAutore(autore);
		return domanda;
	}
	
	private DomandaBean populateCategorie(DomandaBean domanda) {
		ArrayList<CategoriaBean> categorie = CategoriaDAO.getCategorieDomandaByIdDomanda(domanda.getId());		
		domanda.setCategorie(categorie);		
		return domanda;
	}
	
	private DomandaBean populateReferencedEntities(DomandaBean domanda) {
		domanda = populateAutore(domanda);
		domanda = populateCategorie(domanda);
		return domanda;
	}
	
	public ArrayList<DomandaBean> getDomandeRecenti(int start, int end){
		ArrayList<DomandaBean> domandeRecenti = DomandaDAO.getDomandeRecenti(start, end);
		ArrayList<DomandaBean> domandePopolate = new ArrayList<DomandaBean>();
		for(DomandaBean domanda : domandeRecenti) {
			domandePopolate.add(populateReferencedEntities(domanda));
		}
		return domandePopolate;
	}
	
	public ArrayList<DomandaBean> getDomandePertinenti(PartecipanteBean utente, int page, int offset){
		ArrayList<CategoriaBean> categorie = CategoriaDAO.getCategorieByUtente(utente.getId());
		ArrayList<DomandaBean> domande = new ArrayList<DomandaBean>();
		for(DomandaBean domanda : DomandaDAO.getDomandePertinenti(categorie, page, offset)) {
			domande.add(this.populateReferencedEntities(domanda));
		}
		return domande;
	}   
	
	public int getNumOfDomandePertinenti(PartecipanteBean utente) {
		int num;
		ArrayList<CategoriaBean> categorie = CategoriaDAO.getCategorieByUtente(utente.getId());
		num = DomandaDAO.getNumeroDomandePertinenti(categorie);
		logger.info("contate="+num);
		return num;
	}
	
	public int getNumeroDomandeByAutore(String idAutore) {
		return DomandaDAO.getNumeroDomandeByAutore(idAutore);
	}
	
	public HashSet<DomandaBean> getDomandeRisposte(PartecipanteBean utente){
		HashSet<DomandaBean> domandeRisposte = new HashSet<DomandaBean>();		
		domandeRisposte.addAll(DomandaDAO.getDomandeRisposte(utente.getId())) ;
		
		return domandeRisposte;
	}
	
	private static Logger logger = Logger.getLogger(DomandeManager.class.getName());
	
	private static final String UPLOAD_PATH = "C:\\uploads\\allegati_domande\\";
}
