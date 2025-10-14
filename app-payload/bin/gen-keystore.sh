#!/usr/bin/env bash

set -e

proj_dir="$(realpath "$(dirname "${BASH_SOURCE[0]}")/../..")"
readonly proj_dir

keytool -genkeypair -v \
	-keystore "$proj_dir/payload-keystore.jks" \
	-alias payload_key \
	-keyalg RSA -keysize 2048 \
	-validity 10000
