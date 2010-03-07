#!/usr/bin/env bash

grails doc
cd docs
git add *
git commit -a -m '.'
git push