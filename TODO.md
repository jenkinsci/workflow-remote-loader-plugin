
TODOs
=====

Before 1.0
---

Code:
* [x] Replace rm calls by rmDir in workflow 1.11
* [x] Replace "loadLib()" by "loadFile()" (etc.) in order to address the comments from @jglick

Documentation:
* [x] Add sample workflow files to this repo
* [x] Reflect new commands in the doc

Medium-term TODOs (ETA is TBD)
---
* [ ] Bulk loading command (proposed by @tfennely)
* [ ] Timestamping support
* [ ] Avoid node provisioning if the library loading is being invoked from the <code>Node</code> closure
* [ ] Node-free implementation with a checkout to Jenkins server's TMP files ("must" for classpaths)
* [ ] Add fromGithub() command
