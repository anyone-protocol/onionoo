# metrics-base

A copy of Tor's [metrics-base](https://gitlab.torproject.org/tpo/metrics/base) at commit
`e11525332d5d2f0b50f17815ba06de7afaec5db8`, with the `submoduleupdate` target removed, so every
build uses exactly these files. `LICENSE` is metrics-base's license.

To move to a newer metrics-base commit:

1. Copy its `java/` directory over this one.
2. Remove the `submoduleupdate` target from `java/base.xml` and the `init` target's dependency on it.
3. Check the jar names that `build.xml` puts on the classpath against the versions in `java/ivy.xml`.
4. Update the commit above.
