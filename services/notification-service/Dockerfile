FROM rust:1-trixie AS builder

RUN apt-get update && apt-get install -y \
    build-essential \
    pkg-config \
    libssl-dev \
 && rm -rf /var/lib/apt/lists/*

WORKDIR app

COPY Cargo.toml Cargo.lock .

RUN cargo fetch

COPY src ./src

RUN cargo build --release


FROM debian:trixie-slim AS runtime

RUN apt-get update && apt-get install -y ca-certificates \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/target/release/notification-service ./app

EXPOSE 8001

CMD ["./app"]
