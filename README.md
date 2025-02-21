export codebase for outside @Claude:

find . -type f \
-not -path "*/target/*" \
-not -path "*/.idea/*" \
-not -path "*/.git/*" \
-not -name "srcs.txt" \
-exec printf "\n%s:\n" {} \; \
-exec grep -Ev '^\s*(//|#|;)|^\s*$' {} \; > srcs.txt