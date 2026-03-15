FROM python:3.12-slim AS build

RUN pip install --no-cache-dir mkdocs-material

WORKDIR /build
COPY . /src/

RUN cp /src/mkdocs.yml . && \
    mkdir -p docs/labs docs/folien docs/musterloesungen && \
    # Labs: flatten to single files so mkdocs uses H1 headings as nav titles
    for dir in /src/labs/*/; do \
      lab_name=$(basename "$dir"); \
      if [ -f "$dir/README.md" ]; then \
        cp "$dir/README.md" "docs/labs/$lab_name.md"; \
      fi; \
    done && \
    # Assignments: flatten alongside labs (if directory exists)
    if [ -d /src/assignments ]; then \
      for dir in /src/assignments/*/; do \
        name=$(basename "$dir"); \
        if [ -f "$dir/README.md" ]; then \
          cp "$dir/README.md" "docs/labs/$name.md"; \
        fi; \
      done; \
    fi && \
    # Slide PDFs
    cp /src/public/*.pdf docs/folien/ 2>/dev/null || true && \
    printf '# Folien zum Download\n\n' > docs/folien/index.md && \
    if ls docs/folien/*.pdf 1>/dev/null 2>&1; then \
      for pdf in docs/folien/*.pdf; do \
        name=$(basename "$pdf" .pdf); \
        echo "- [${name}](${name}.pdf)" >> docs/folien/index.md; \
      done; \
    else \
      echo "*Keine Folien verfügbar.*" >> docs/folien/index.md; \
    fi && \
    # Zip solutions
    if [ -d /src/solutions ]; then \
      cd /src && python3 -c "import zipfile,os;zf=zipfile.ZipFile('/build/docs/musterloesungen/solutions.zip','w',zipfile.ZIP_DEFLATED);[zf.write(os.path.join(r,f)) for r,d,fs in os.walk('solutions') for f in fs];zf.close()" && cd /build; \
      printf '# Musterlösungen zum Download\n\n' > docs/musterloesungen/index.md; \
      printf '[Musterlösungen herunterladen](solutions.zip){ .md-button }\n' >> docs/musterloesungen/index.md; \
    fi && \
    # Generate TOC index page
    printf '# DDD & Clean Architecture mit Spring Boot 4\n\n' > docs/index.md && \
    printf '## Inhaltsverzeichnis\n\n### Labs\n\n' >> docs/index.md && \
    for f in docs/labs/*.md; do \
      title=$(head -1 "$f" | sed 's/^# //'); \
      name=$(basename "$f" .md); \
      echo "- [${title}](labs/${name}.md)" >> docs/index.md; \
    done && \
    printf '\n### [Folien zum Download](folien/)\n\n' >> docs/index.md && \
    printf '### [Musterlösungen zum Download](musterloesungen/)\n' >> docs/index.md && \
    # Generate explicit nav from H1 headings
    printf '\nnav:\n  - Startseite: index.md\n' >> mkdocs.yml && \
    printf '  - Folien zum Download: folien/index.md\n' >> mkdocs.yml && \
    printf '  - Musterlösungen zum Download: musterloesungen/index.md\n' >> mkdocs.yml && \
    printf '  - Labs:\n' >> mkdocs.yml && \
    for f in docs/labs/*.md; do \
      title=$(head -1 "$f" | sed 's/^# //'); \
      name=$(basename "$f" .md); \
      printf '    - "%s": labs/%s.md\n' "$title" "$name" >> mkdocs.yml; \
    done && \
    rm -rf /src

RUN mkdocs build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/site /usr/share/nginx/html
