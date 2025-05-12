package skillfactory.specialinstruments.services.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;
import java.util.Enumeration;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
        CachedBodyHttpServletResponse wrappedResponse = new CachedBodyHttpServletResponse(response);

        long start = System.currentTimeMillis();

        log.info("\nREQUEST:\nmethod={},\nuri={},\nparams={},\nheaders={},\nbody={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                getHeaders(request),
                new String(wrappedRequest.getCachedBody()));

        filterChain.doFilter(wrappedRequest, wrappedResponse);

        long duration = System.currentTimeMillis() - start;

        log.info("\nRESPONSE:\nstatus={},\nduration={}ms,\nbody={}",
                response.getStatus(),
                duration,
                new String(wrappedResponse.getCachedBody()));

        wrappedResponse.copyBodyToResponse();
    }

    private String getHeaders(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            sb.append(headerName).append(": ").append(headerValue).append("\n");
        }
        return sb.toString();
    }


    @Getter
    public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            InputStream requestInputStream = request.getInputStream();
            this.cachedBody = requestInputStream.readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) { }

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

    }

    public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

        private ByteArrayOutputStream cachedContent = new ByteArrayOutputStream();
        private ServletOutputStream outputStream;
        private PrintWriter writer;

        public CachedBodyHttpServletResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (this.writer != null) {
                throw new IllegalStateException("getWriter() has already been called on this response.");
            }
            if (this.outputStream == null) {
                this.outputStream = new ServletOutputStream() {
                    @Override
                    public void write(int b) {
                        cachedContent.write(b);
                    }
                    @Override
                    public boolean isReady() { return true; }
                    @Override
                    public void setWriteListener(WriteListener listener) {}
                };
            }
            return this.outputStream;
        }

        @Override
        public PrintWriter getWriter() {
            if (this.outputStream != null) {
                throw new IllegalStateException("getOutputStream() has already been called on this response.");
            }
            if (this.writer == null) {
                this.writer = new PrintWriter(new OutputStreamWriter(cachedContent));
            }
            return this.writer;
        }

        public byte[] getCachedBody() {
            if (writer != null) {
                writer.flush();
            }
            return cachedContent.toByteArray();
        }

        public void copyBodyToResponse() throws IOException {
            ServletOutputStream out = super.getOutputStream();
            out.write(getCachedBody());
            out.flush();
        }
    }
}
