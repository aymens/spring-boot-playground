#!/bin/bash

# Find all files and extract content while excluding certain paths
find . -type f \
  -not -path "*/target/*" \
  -not -path "*/.idea/*" \
  -not -path "*/.git/*" \
  -not -name "srcs.txt" \
  -exec printf "\n%s:\n" {} \; \
  -exec grep -Ev '^\s*(//|#|;)|^\s*$' {} \; > srcs.txt