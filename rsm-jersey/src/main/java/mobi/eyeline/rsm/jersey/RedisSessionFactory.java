package mobi.eyeline.rsm.jersey;

import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static mobi.eyeline.rsm.jersey.RedisSession.NULL_SESSION;

public class RedisSessionFactory implements Factory<RedisSession> {

  private final Logger log = Logger.getLogger(RedisSessionFactory.class.getName());

  private final RedisSessionManager manager;
  private final HttpHeaders headers;

  @Inject
  public RedisSessionFactory(RedisSessionManager manager, HttpHeaders headers) {
    if (log.isLoggable(Level.FINEST)) {
      log.finest("RedisSessionFactory initialized");
    }

    this.manager = manager;
    this.headers = headers;
  }

  @Override
  public RedisSession provide() {
    final String sessionId = headers.getHeaderString(manager.getSessionCookieName());

    if (sessionId == null || sessionId.isEmpty()) {
      if (log.isLoggable(Level.FINEST)) {
        log.finest("No session ID present in request");
      }

      return NULL_SESSION;

    } else {
      if (log.isLoggable(Level.FINEST)) {
        log.finest("Loading session ID = [" + sessionId + "]");
      }

      try {
        return manager.findSession(sessionId);

      } catch (IOException e) {
        log.log(Level.WARNING, "Failed loading session for ID = [" + sessionId + "]", e);
        return NULL_SESSION;
      }
    }
  }

  @Override
  public void dispose(RedisSession session) {
    if (session == NULL_SESSION) {
      return;
    }

    if (log.isLoggable(Level.FINEST)) {
      log.finest("Detaching session: " + session);
    }

    manager.afterRequest((RedisSessionImpl) session);
  }

}