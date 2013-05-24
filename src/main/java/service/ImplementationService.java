package main.java.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import main.java.orm.InterfaceCompanyDAO;
import main.java.orm.InterfaceComputerDAO;
import main.java.pojo.Computer;
import main.java.pojo.Page;

@Service
@Scope("singleton")
@Transactional(readOnly = true)
public class ImplementationService implements InterfaceService {

	private static final Logger logger = LoggerFactory
			.getLogger(ImplementationService.class);
	
	@Autowired
	InterfaceCompanyDAO companyDAO;
	@Autowired
	InterfaceComputerDAO computerDAO;

public ImplementationService() {
	}
	
	public InterfaceCompanyDAO getDAOcompany() {
		return companyDAO;
	}

	public void setDAOcompany(InterfaceCompanyDAO dAOcompany) {
		companyDAO = dAOcompany;
	}

	public InterfaceComputerDAO getImplDAO() {
		return computerDAO;
	}

	public void setImplDAO(InterfaceComputerDAO implDAO) {
		this.computerDAO = implDAO;
	}

	@Override
	public Page ConstructionTableauAccueil(HttpServletRequest request) throws Exception {

		Page page = new Page();

		page.setS(UtilitaireService.gestionNull(request.getParameter("s")));
		page.setP(UtilitaireService.gestionNull(request.getParameter("p")));
		page.setStarter(UtilitaireService.gestionStarter(page.getP()));
		page.setF(UtilitaireService.gestionNullClause(request.getParameter("f")));

		page.setTailleTable(computerDAO.getSizeComputers("%" + page.getF() + "%"));

		page.setComputers(computerDAO.getListComputersSlice(page.getStarter(),
				page.getS(), "%" + page.getF() + "%"));
		return page;
	}

	@Override
	@Transactional(readOnly = false)
	public void DeleteComputer(HttpServletRequest request) throws Exception{

		Integer id = Integer.parseInt(request.getParameter("id"));
		try {
			computerDAO.deleteComputerByID(id);
		} catch (Exception e) {
			logger.error("Erreur de suppression d'un computer" + e.getMessage());
			throw e;}

	}

	@Override
	public Page ModifyOrAddComputer(HttpServletRequest request) throws Exception{

		Page page = new Page();

		page.setCompanies(companyDAO.getListCompanies());

		if (request.getParameter("id") == null) {
			page.setUrl("/jsp/NewComputer.jsp");
		} else {
			Integer id = Integer.parseInt(request.getParameter("id"));
			page.setCp(computerDAO.getComputerByID(id));
			page.setUrl("/jsp/Computer.jsp");
		}
		return page;
	}

	@Transactional(readOnly = false)
	public boolean SaveComputer(HttpServletRequest request) throws Exception{

		boolean error = false;
		String name = null;
		// name
		if (request.getParameter("name") == null
				|| request.getParameter("name").trim().length() == 0) {
			error = true;
			request.setAttribute("nameError", "error");
		} else {
			name = request.getParameter("name");
		}

		// compagnie
		// Inutile car ne dépend pas de l'utilisateur il ne peut faire
		// d'injection
		String company_id = request.getParameter("company");

		// dates
		SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
		df.applyPattern("yyyy-MM-dd");
		df.setLenient(false);
		Calendar introduced = Calendar.getInstance();
		Calendar discontinued = Calendar.getInstance();

		if (request.getParameter("introduced").isEmpty()) {
			introduced = null;
		} else {
			try {
				introduced
						.setTime(df.parse(request.getParameter("introduced")));
			} catch (ParseException e) {
				error = true;
				request.setAttribute("introducedError", "error");
			}
		}

		if (request.getParameter("discontinued").isEmpty()) {
			discontinued = null;
		} else {
			try {
				discontinued.setTime(df.parse(request
						.getParameter("discontinued")));
			} catch (ParseException e) {
				error = true;
				request.setAttribute("discontinuedError", "error");
			}
		}

		if (!error) {
			Computer cp;

			boolean newCp;

			if (request.getParameter("id") == null) {
				cp = new Computer(name, introduced, discontinued, company_id);
				newCp = true;
			} else {
				String id = request.getParameter("id");
				cp = new Computer(id, name, introduced, discontinued,
						company_id);
				newCp = false;

			}

			try {
				computerDAO.saveComputer(cp, newCp);
			} catch (Exception e) {
				logger.error("Erreur de sauvegarde des ordinateurs"
						+ e.getMessage());
				throw e;
			}

		}

		return error;

	}

}
