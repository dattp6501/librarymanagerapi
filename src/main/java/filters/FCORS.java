package filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import global.Init;


@WebFilter(urlPatterns = {"/*"})
public class FCORS implements Filter{
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");
        System.out.println("START BEGIN ALL FILTER");
        System.out.println("IP CLIENT: " + getClientIp(req));
        countUser(getClientIp(req));
        System.out.println("END BEGIN ALL FILTER");
        
        chain.doFilter(req, resp);
    }
    private static String getClientIp(ServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = ((HttpServletRequest) request).getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    private void countUser(String ip){
        if(!Init.SET_IP.contains(ip)){
            Init.SET_IP.add(ip);
        }
        System.out.println("USER NUMBER: "+Init.SET_IP.size());
    }
}
