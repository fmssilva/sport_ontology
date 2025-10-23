# 🔧 Test Configuration Guide

## Quick Configuration Changes

To enable/disable specific test suites, edit `config/TestConfig.java`:

### Test Suites:
- `RUN_BASIC_INTEGRITY_TESTS = true/false` - Core OBDA validation tests
- `RUN_OWA_CWA_DEMO_TESTS = true/false` - Open vs Closed World demos  
- `RUN_PERFORMANCE_BENCHMARKS = false` - Performance measurement tests (future)
- `RUN_REASONING_TESTS = false` - Advanced reasoning validation (future)

### Configuration Options:
- `ENABLE_TIMING = true/false` - Show execution times
- `ENABLE_VERBOSE_OUTPUT = true/false` - Detailed output
- `ENABLE_CROSS_VALIDATION = true/false` - SQL ↔ SPARQL validation
- `STOP_ON_FIRST_FAILURE = false` - Stop immediately on test failure

### Performance Thresholds:
- `SQL_PERFORMANCE_THRESHOLD = 100` - Warn if SQL > 100ms
- `SPARQL_PERFORMANCE_THRESHOLD = 5000` - Warn if SPARQL > 5000ms

## Quick Configurations:

### Development Mode (Fast Testing):
```java
public static final boolean RUN_BASIC_INTEGRITY_TESTS = true;
public static final boolean RUN_OWA_CWA_DEMO_TESTS = false;
```

### Demo Mode (Show Everything):
```java
public static final boolean RUN_BASIC_INTEGRITY_TESTS = true;
public static final boolean RUN_OWA_CWA_DEMO_TESTS = true;
```

### Performance Testing Mode:
```java
public static final boolean RUN_BASIC_INTEGRITY_TESTS = true;
public static final boolean RUN_PERFORMANCE_BENCHMARKS = true;
```

After making changes, just run `./run-tests.bat` (Windows) or `./run-tests.sh` (Linux/Mac).