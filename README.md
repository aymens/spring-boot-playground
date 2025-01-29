```bash
mvn clean package -pl sba -am
```
```bash
docker build -t sba -f sba/Dockerfile sba/
```

-t sba: Tag/name the resulting image as "sba"

-f sba/Dockerfile: Specify the location of the Dockerfile to use

sba/: The build context - the directory path sent to Docker daemon as the context for building


//dep

resources:
limits:
memory: 512Mi      # Upper bound
cpu: 500m         # Half a CPU core
requests:
memory: 256Mi     # Initial request
cpu: 200m        # 0.2 CPU core