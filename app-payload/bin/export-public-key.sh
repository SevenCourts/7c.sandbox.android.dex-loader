#!/usr/bin/env bash

set -e

proj_dir="$(realpath "$(dirname "${BASH_SOURCE[0]}")/../..")"
readonly proj_dir

keytool -export -rfc \
	-keystore "$proj_dir/payload-keystore.jks" \
	-alias payload_key \
	-file "$proj_dir/public_key.cer"
