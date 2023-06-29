#!/bin/bash

# input working dir
wd="$1"

while true; do
  # detect shutdown file to shutdown
  if [ -f "${wd}shutdown" ]; then
    break
  fi
  # detect run.sh file to run script
  if [ -f "${wd}run.sh" ]; then
    bash "${wd}run.sh"
    # only proc0 to remove it
    if [ "${SLURM_PROCID}" -eq 0 ]; then
      rm "${wd}run.sh"
    fi
  fi
  sleep 0.1s
done
