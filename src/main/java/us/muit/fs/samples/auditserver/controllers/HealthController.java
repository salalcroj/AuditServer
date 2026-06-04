package us.muit.fs.samples.auditserver.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import us.muit.fs.a4i.model.remote.GitHubRepositoryEnquirer;
import us.muit.fs.a4i.model.remote.RemoteEnquirer;
import us.muit.fs.a4i.model.entities.Metric;
import us.muit.fs.samples.auditserver.config.AppProperties;

@RestController
public class HealthController {
	@Autowired
	private AppProperties config;
	private static Logger log = Logger.getLogger(HealthController.class.getName());
	
	private String getHealthzGithubRepo()
	{
		return config.getHealthzGithubRepo();
	}
	
	@RequestMapping("/readyz")
	@GetMapping(path = "/readyz", produces=MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Map<String, Object>> healthz() {
		try {
			String healthzGithubRepo = this.getHealthzGithubRepo();
			Map<String, Object> body = new HashMap<>();
			RemoteEnquirer remote = new GitHubRepositoryEnquirer();
			
			Metric myMetric = remote.getMetric("totalAdditions", healthzGithubRepo);
			
			if (((Integer)myMetric.getValue() != 0) && (myMetric.getName().equals("totalAdditions"))) {
				body.put("healthy", true);
				body.put("totalAdditions", myMetric.getValue());
				body.put("metric", myMetric);
				body.put("remoteRepo", healthzGithubRepo);
				log.fine("La respuesta recibida ha sido: " + myMetric);
			} else {
				log.fine("La respuesta del remoto no se ha recibido bien");
				body.put("healthy", false);
				body.put("remoteRepo", healthzGithubRepo);
			}
			return ResponseEntity.status(HttpStatus.OK).body(body);
		} catch(Exception ref) {
			Map<String, Object> body = new HashMap<>();
			log.fine("Se ha recibido esta excepción: " + ref);
			
			body.put("healthy", false);
			body.put("error", ref.getMessage());
			
			// FUERZA BRUTA PARA EL EJERCICIO: Introducir el repositorio aunque la red falle
			try {
				body.put("remoteRepo", this.getHealthzGithubRepo());
			} catch(Exception e) {
				body.put("remoteRepo", "unknown");
			}
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
		}
	}
}