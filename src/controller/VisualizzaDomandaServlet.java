package controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileUtils;

import model.AllegatiHandler;
import model.DomandaBean;
import model.DomandeManager;
import model.MotivazioneBean;
import model.MotivazioneDAO;
import model.PartecipanteBean;
import model.RispostaBean;
import model.RispostaDAO;
import model.RisposteManager;
import model.UtenteBean;
import model.UtenteDAO;
import model.VotazioneBean;
import model.VotazioneDAO;

/**
 * Servlet implementation class VisualizzaDomandaServlet
 */
@WebServlet("/VisualizzaDomandaServlet")
public class VisualizzaDomandaServlet extends CustomServlet {
	private static final long serialVersionUID = 1L;
	Logger log = Logger.getAnonymousLogger();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public VisualizzaDomandaServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String idDomanda = request.getParameter("id");
		
		PrintWriter writer = response.getWriter();
		
		if(idDomanda != null) {
			
			try {
				
				DomandeManager manager = new DomandeManager();
				PartecipanteBean user = (PartecipanteBean) request.getSession().getAttribute("utenteLoggato");
				DomandaBean domandaVisualizzata = manager.getDomandaById(idDomanda);
				
				if(domandaVisualizzata != null) {
					
					request.setAttribute("domanda", domandaVisualizzata);
					
					// allegati
					/* 
					 * Li metto all'interno di un ArrayList perch� con un array normale ci sono problemi nella JSP.
					 * Il problema consiste in ${allegati.length > 0} che da errore. Con ${allegati.size() > 0} funziona.
					 */
					
					ArrayList<RispostaBean> risposte = new ArrayList<RispostaBean>();
					ArrayList<MotivazioneBean> motivazioni = new ArrayList<MotivazioneBean>();
					
					motivazioni=MotivazioneDAO.getAll();
					request.setAttribute("motivazioni", motivazioni);
											
					int page = 0;
					if(request.getParameter("pageRi") != null) {
						log.info("Pagina numero: "+request.getParameter("pageRi"));		
						page = Integer.parseInt(request.getParameter("pageRi"));	
					}
					
					
					//risposte=RispostaDAO.getRisposteByIdDomanda(idDomanda, page);//aggiunta
					risposte=RisposteManager.getRisposteByIdDomanda(idDomanda, page);
					

					RisposteManager managerRisposte = new RisposteManager();
					UtenteBean utenteloggato = getLoggedUser(request.getSession());
					//request.setAttribute("risposteApprezzate", managerRisposte.getRisposteApprezzate(utenteloggato));
					HashSet<RispostaBean> rsrb =  managerRisposte.getRisposteApprezzate(utenteloggato);
					HashSet<RispostaBean> rsrb2 =  managerRisposte.getRisposteNonApprezzate(utenteloggato);


					
					 log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
					 log.info("LA SIZE DELL'HASHSET E' "+ String.valueOf(rsrb.size()));
					 
					 HashSet<String> risposteApprezzate = new HashSet<String>();
					 HashSet<String> risposteNonApprezzate = new HashSet<String>();

					 
						for (RispostaBean x : rsrb) {
							 risposteApprezzate.add(x.getId());
							 log.info(x.getId());
						}
						
						for (RispostaBean y : rsrb2) {
							 risposteNonApprezzate.add(y.getId());
							 log.info(y.getId());
						}
						
						
						request.setAttribute("risposteApprezzate", risposteApprezzate);
						request.setAttribute("risposteNonApprezzate", risposteNonApprezzate);

						
						/*rsrb.forEach((k) -> {
							log.info(k.getId());
						});*/
					 
					 log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

					boolean b = RispostaDAO.getRisposteByIdDomanda(idDomanda, page+1).isEmpty();
					if(b) {
						//log.info("La prossima scheda � vuota");
						request.setAttribute("next", 0);
					}else {
						//log.info("La prossima scheda � piena");
						request.setAttribute("next", 1);
					}
					
					request.setAttribute("risposte", risposte);
					
					/* Per sapere se l'utente � loggato E non � l'autore della domanda e quindi se pu� apparirgli o meno il form per pubblicare la risposta. */
					UtenteBean utente = getLoggedUser(request.getSession());
					request.setAttribute("utenteLoggato", utente);
					
					RequestDispatcher dispatcher = request.getRequestDispatcher("/Domanda.jsp");
					dispatcher.forward(request, response);
					
				} else {
					writer.print("Domanda con id = '" + idDomanda + "' non trovata.");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				writer.print(e.getMessage());
			}
			
		} else {
			writer.print("L'ID della domanda non pu� essere nullo.");
		}
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
