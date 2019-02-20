# Atom GitConvention #
The goal of the convention is to introduce simple, transparent and effective rules for working with Git.

Development of Atom is based on [Git Flow](https://leanpub.com/git-flow/read). Details below.

## Branching ##

| Branch name | Value branches | Source thread | Branch example |
| ------------- | ------------- | ------------- | ------------- |
| **master**| Completely ready for production | **release**| |
| **develop**| Development of new functionality | **master**| |
| **release**| Testing all new features | **develop**| |
| | | | |
| **bugfix-***| Corrects bug of new functionality | **release**| *bugfix-auth* |
| **feature-***| Adds a new feature | **develop**| *feature-auth* |
| **hotfix-***| Makes an urgent fix for production | **master**| *hotfix-auth* |
-----
![Image of GitFlow](https://i.ytimg.com/vi/w2r0oLFtXAw/maxresdefault.jpg)
-----

## Сommits ##
**Fundamental rules:**
1. All commits should be in English.
2. It is forbidden to use past tense.
3. Be sure to use the prefix.
4. In the end there should not be an extra punctuation mark.
5. The length of any part must not exceed 100 characters.

**Structure:**
``
[Prefix] <Message>
``

| Prefix | Value | Example |
| ------- | -------- | ------ |
| **[FIX]**| All that concerns the correction of bugs | [FIX] Authorization Failed |
| **[DOCS]**| Everything related to documentation | [DOCS] Documenting Authorization APIs |
| **[FEATURE]**| All that concerns new features | [FEATURE] 2FA with authorization |
| **[STYLE]**| All that concerns typos and formatting | [STYLE] Mistakes in the authorization module |
| **[REFACTOR]**| Everything related to refactoring | [REFACTOR] Switch to EDA in the authorization module |
| **[TEST]**| Everything related to testing | [TEST] Coverage of the authorization module tests |
| **[ANY]**| Anything that does not fit the previous one. | [ANY] Travis CI Connection |