
// Declare functions generated by toolchain to initialize components.
void __component_global_init(void);
void __component_global_shutdown(void);

// -----------------------------------------------------------------------------
// Implementation of the main interface.
// -----------------------------------------------------------------------------

// int main(int argc, string[] argv)
int main(int argc, char *argv[]) {
	int r;
	// call global_init function. (this function calls constructors of static
	// component instances)
	__component_global_init();

	// call the "main" entry point of the application
	r = CALL(entryPoint, main)(argc, argv);

	// call global_shputdown function. (this function calls destructors of static
	// component instances)
	__component_global_shutdown();

	return r;
}
