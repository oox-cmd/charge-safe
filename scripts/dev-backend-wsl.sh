#!/usr/bin/env bash

set -euo pipefail

if ! command -v ip >/dev/null 2>&1; then
  echo "错误：找不到 ip 命令，无法获取 Windows 主机地址。" >&2
  exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "错误：找不到 mvn，请先安装并配置 Maven 3.8+。" >&2
  exit 1
fi

windows_host="$(ip route show default | awk '{print $3; exit}')"

if [[ -z "${windows_host}" ]]; then
  echo "错误：无法从 WSL 默认路由获取 Windows 主机地址。" >&2
  exit 1
fi

# 仅限本地开发和课程演示，不得用于生产或云端数据库。
export CHARGE_SAFE_DB_HOST="${CHARGE_SAFE_DB_HOST:-${windows_host}}"
export CHARGE_SAFE_DB_PORT="${CHARGE_SAFE_DB_PORT:-3306}"
export CHARGE_SAFE_DB_NAME="${CHARGE_SAFE_DB_NAME:-charge_safe_dev}"
if [ -z "${CHARGE_SAFE_DB_USERNAME:-}" ]; then
  echo "CHARGE_SAFE_DB_USERNAME must be set before starting the backend." >&2
  exit 1
fi
if [ -z "${CHARGE_SAFE_DB_PASSWORD:-}" ]; then
  echo "CHARGE_SAFE_DB_PASSWORD must be set before starting the backend." >&2
  exit 1
fi
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd -- "${script_dir}/.." && pwd)"

cd "${repo_root}/charge-safe-backend"
exec mvn spring-boot:run \
  -Dspring-boot.run.profiles="${SPRING_PROFILES_ACTIVE}"
