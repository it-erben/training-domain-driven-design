FROM python:3.12-slim AS build

RUN pip install --no-cache-dir mkdocs-material

WORKDIR /build
COPY . /src/

RUN cp /src/mkdocs.yml . && \
    # Root README as landing page
    mkdir -p docs && \
    cp /src/README.md docs/index.md && \
    # Labs: copy only markdown, rename README.md -> index.md for clean URLs
    find /src/labs -name "*.md" | while read f; do \
      rel="${f#/src/labs/}"; \
      target="docs/labs/$rel"; \
      if [ "$(basename "$rel")" = "README.md" ]; then \
        target="docs/labs/$(dirname "$rel")/index.md"; \
      fi; \
      mkdir -p "$(dirname "$target")"; \
      cp "$f" "$target"; \
    done && \
    # Assignments: copy only markdown (directory may not exist)
    if [ -d /src/assignments ]; then \
      find /src/assignments -name "*.md" | while read f; do \
        rel="${f#/src/assignments/}"; \
        target="docs/assignments/$rel"; \
        if [ "$(basename "$rel")" = "README.md" ]; then \
          target="docs/assignments/$(dirname "$rel")/index.md"; \
        fi; \
        mkdir -p "$(dirname "$target")"; \
        cp "$f" "$target"; \
      done; \
    fi && \
    # Slide PDFs from CI artifacts (pdf-publisher writes to public/)
    mkdir -p docs/slides && \
    cp /src/public/*.pdf docs/slides/ 2>/dev/null || true && \
    # Generate slides index linking all PDFs
    if ls docs/slides/*.pdf 1>/dev/null 2>&1; then \
      printf '# Slides\n\n' > docs/slides/index.md; \
      for pdf in docs/slides/*.pdf; do \
        name=$(basename "$pdf" .pdf); \
        echo "- [${name}](${name}.pdf)" >> docs/slides/index.md; \
      done; \
    fi && \
    rm -rf /src

RUN mkdocs build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/site /usr/share/nginx/html
