# GraalVM filter tests

A small application which contains a reflection call.
The call stack is as follows:

`Main` -> static call-> `A` -> static call -> `C` -> reflect call -> `B`

There is a test called `AppTest` in the test sources which uses reflection to create an instance of `App` and calls the `main` method.

# user-code-filter.json

Influences the class in the `typeReachable` clause.
The nearest included class in the reflection stack trace is used.

Example: Setting the filter to this:

```json
{
    "rules": [
        {
            "excludeClasses": "**"
        },
        {
            "includeClasses": "graal.filter.test.C"
        }
    ]
}
```

generates this `reflection-config.json`:

```json
[
    {
        "name": "graal.filter.test.B",
        "condition": {
            "typeReachable": "graal.filter.test.C"
        },
        "methods": [
            {
                "name": "<init>",
                "parameterTypes": [
                ]
            },
            {
                "name": "someMethod",
                "parameterTypes": [
                ]
            }
        ]
    }
]
```

Note that the `typeReachable` is set to `graal.filter.test.C`.

Setting the filter to this:

```json
{
    "rules": [
        {
            "excludeClasses": "**"
        },
        {
            "includeClasses": "graal.filter.test.A"
        }
    ]
}
```

results in this `reflect-config.json`:

```json
[
    {
        "name": "graal.filter.test.B",
        "condition": {
            "typeReachable": "graal.filter.test.A"
        },
        "methods": [
            {
                "name": "<init>",
                "parameterTypes": [
                ]
            },
            {
                "name": "someMethod",
                "parameterTypes": [
                ]
            }
        ]
    }
]
```

Note that `typeReachable` now points to `A`, as this is the nearest included class in our config.

If neither `A` nor `C` is in the `includeClasses`, no reflection call is recorded.

**Recommendation**: Exclude all classes, and only include classes from your production packages:

```json
{
    "rules": [
        {
            "excludeClasses": "**"
        },
        {
            "includeClasses": "graal.filter.test.**"
        }
    ]
}
```

If the tests do reflection which would be recorded by the agent, read on.

If the tests are in a separate package from the production code, exclude the whole test package:

```json
{
    "rules": [
        {
            "excludeClasses": "**"
        },
        {
            "includeClasses": "your.production.package.**"
        },
        {
            "excludeClasses": "your.test.package.**"
        }
    ]
}
```

If the tests are in the same package as your production code, you can use `regexRules` to exclude them:

```json
{
    "rules": [
        {
            "excludeClasses": "**"
        },
        {
            "includeClasses": "your.package.**"
        }
    ],
    "regexRules": [
        {
            "excludeClasses": "\\Qyour.package.\\E.*Test"
        }
    ]
}
```

This will exclude all test classes in `your.package` (and subpackages) ending in `Test`.
The reflection calls by your production code will still be recorded.

# caller-filter.json

Checks if the direct class which does the reflection call is included or excluded.
If it's excluded, the reflection call is not recorded.
**Important**: It has to be the direct class which does the call, not one which is in the call stack.

Example:

this `caller-filter.json`:

```json
{
    "rules": [
        {
            "excludeClasses": "**"
        },
        {
            "includeClasses": "graal.filter.test.C"
        }
    ]
}
```

records the usage of the reflection call to `B`, because `C` is the one which does it.

This `caller-filter.json`:

```json
{
    "rules": [
        {
            "excludeClasses": "**"
        },
        {
            "includeClasses": "graal.filter.test.A"
        }
    ]
}
```

does not record the call, as `A` calls `C` which does the reflection.
`A` may be in the call stack, but that doesn't matter.
The `includeClasses` only takes the direct caller into account.

That means if your code is using some library which does the reflection calls, they would not be included without additional configuration.

You could use that file to find all reflection calls which originates from your code, excluding all 3rd party calls.

# access-filter.json

Checks if the target of the reflection is included.
In contrast to `caller-filter.json` this filter acts on the *target* of the reflection.

This `access-filter.json`:

```json
{
    "rules": [
        {
            "excludeClasses": "**"
        },
        {
            "includeClasses": "graal.filter.test.B"
        }
    ]
}
```

will only record reflection calls which have `B` as a target.
You could use that file to find all reflection calls to types inside your code, excluding calls to 3rd party types.

# Running it yourself

Run

```
./gradlew -Pagent clean test
```

and check the generated files under `app/build/native/agent-output/test`.

You can copy the collected metadata to `src/main/resources` with:

```
./gradlew metadataCopy
```

and then a:

```
./gradlew nativeRun
```

works.
