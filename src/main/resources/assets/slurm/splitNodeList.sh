#!/bin/bash

# 输入字符串
input="$1"
output=()

# Remove the "cn", "[" and "]" characters
trimmedStr="${input#cn}"
trimmedStr="${trimmedStr#\[}"
trimmedStr="${trimmedStr%\]}"

# Split the string by comma
IFS=',' read -ra array <<< "$trimmedStr"

# Loop through each range and generate the numbers
for range in "${array[@]}"; do
  if [[ "$range" =~ ([0-9]+)-([0-9]+) ]]; then
    # Range of numbers
    start="${BASH_REMATCH[1]}"
    end="${BASH_REMATCH[2]}"
    for ((i=start; i<=end; i++)); do
      output+=("cn$i")
    done
  else
    # Single number
    output+=("cn$range")
  fi
done

# output
echo "${output[@]}"
