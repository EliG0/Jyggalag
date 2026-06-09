import json
import os


def convert_md_to_json(md_file_path, json_file_path):
    greetings_list = []
    current_tag = None

    if not os.path.exists(md_file_path):
        print(f"{md_file_path} не найден.")
        return
    with open(md_file_path, "r", encoding="utf-8") as md_file:
        for line in md_file:
            line = line.strip()

            if not line:
                continue

            if line.startswith("#"):
                current_tag = line.lstrip("#").strip().lower()
                continue

            if current_tag:
                greetings_list.append({"tag": current_tag, "text": line})

    with open(json_file_path, "w", encoding="utf-8") as json_file:
        json.dump(greetings_list, json_file, ensure_ascii=False, indent=2)

    print(
        f"Done: {json_file_path}"
    )

convert_md_to_json("greetings.md", "../greetings.json")