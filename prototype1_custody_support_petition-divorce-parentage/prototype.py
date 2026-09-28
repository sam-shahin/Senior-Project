import json
from pathlib import Path

# Prototype 1 terminal test
# Expected layout:
#
# project/
#   prototype.py
#   data/
#     legal_knowledge/
#       divorce.json
#       parentage.json
#       custody_support_petition.json
#       sources.json
#     pathways/
#       family_law_tree.json
#
# If your source/pathway filenames differ, change the constants below.

BASE_DIR = Path(__file__).resolve().parent
LEGAL_DIR = BASE_DIR / "data" / "legal_knowledge"
PATHWAY_FILE = BASE_DIR / "data" / "pathways" / "family_law_tree.json"
SOURCE_FILE = LEGAL_DIR / "sources.json"

LEGAL_FILES = [
    path for path in LEGAL_DIR.glob("*.json")
    if path.name != "sources.json"
]

def load_json(path):
    """Load one JSON file and give a useful error if it cannot be read."""
    try:
        with path.open("r", encoding="utf-8") as file:
            return json.load(file)
    except FileNotFoundError:
        raise SystemExit(f"\nERROR: File not found:\n{path}\n")
    except json.JSONDecodeError as error:
        raise SystemExit(
            f"\nERROR: Invalid JSON in:\n{path}\n"
            f"Line {error.lineno}, column {error.colno}: {error.msg}\n"
        )


def build_index(items, item_type):
    """Turn a list of objects with IDs into {id: object} for fast lookup."""
    index = {}

    for item in items:
        item_id = item.get("id")

        if not item_id:
            raise SystemExit(f"ERROR: A {item_type} object is missing an id.")

        if item_id in index:
            raise SystemExit(f"ERROR: Duplicate {item_type} id: {item_id}")

        index[item_id] = item

    return index


def load_knowledge():
    """Load all legal files and combine their objects into lookup dictionaries."""
    definitions = {}
    laws = {}
    procedures = {}

    for path in LEGAL_FILES:
        data = load_json(path)

        for object_type, target in [
            ("definitions", definitions),
            ("laws", laws),
            ("procedures", procedures),
        ]:
            for item in data.get(object_type, []):
                item_id = item.get("id")

                if not item_id:
                    raise SystemExit(
                        f"ERROR: Object in {path.name}/{object_type} has no id."
                    )

                if item_id in target:
                    raise SystemExit(f"ERROR: Duplicate knowledge id: {item_id}")

                target[item_id] = item

    return {
        "definitions": definitions,
        "laws": laws,
        "procedures": procedures,
    }


def validate_pathway(pathway, knowledge, sources):
    """Check references before the user starts navigating."""
    nodes = build_index(pathway.get("nodes", []), "node")
    errors = []

    start_node = pathway.get("start_node")
    if start_node not in nodes:
        errors.append(f"start_node does not exist: {start_node}")

    for node_id, node in nodes.items():
        refs = node.get("knowledge_refs", {})

        for ref_id in refs.get("definitions", []):
            if ref_id not in knowledge["definitions"]:
                errors.append(f"{node_id}: missing definition {ref_id}")

        for ref_id in refs.get("laws", []):
            if ref_id not in knowledge["laws"]:
                errors.append(f"{node_id}: missing law {ref_id}")

        for ref_id in refs.get("procedures", []):
            if ref_id not in knowledge["procedures"]:
                errors.append(f"{node_id}: missing procedure {ref_id}")

        for option in node.get("options", []):
            next_node = option.get("next_node")
            if next_node not in nodes:
                errors.append(f"{node_id}: missing next_node {next_node}")

    # Validate source references used by every legal object.
    for category in ("definitions", "laws", "procedures"):
        for item_id, item in knowledge[category].items():
            for source_id in item.get("source_ids", []):
                if source_id not in sources:
                    errors.append(f"{item_id}: missing source {source_id}")

    if errors:
        print("\nVALIDATION FAILED")
        print("-" * 60)
        for error in errors:
            print(f"- {error}")
        raise SystemExit("\nFix the references above and run the program again.\n")

    return nodes


def retrieve_node_knowledge(node, knowledge):
    """Retrieve every legal object explicitly referenced by the current node."""
    refs = node.get("knowledge_refs", {})

    return {
        "definitions": [
            knowledge["definitions"][item_id]
            for item_id in refs.get("definitions", [])
        ],
        "laws": [
            knowledge["laws"][item_id]
            for item_id in refs.get("laws", [])
        ],
        "procedures": [
            knowledge["procedures"][item_id]
            for item_id in refs.get("procedures", [])
        ],
    }


def source_lines(source_ids, sources):
    """Return simple source strings for Prototype 1."""
    lines = []

    for source_id in source_ids:
        source = sources[source_id]
        citation = source.get("citation") or source.get("title") or source_id
        url = source.get("url", "")
        lines.append(f"Source: {citation}" + (f"\n        {url}" if url else ""))

    return lines


def print_response(node, retrieved, sources):
    """Prototype 1 response compilation: print everything retrieved."""
    print("\n" + "=" * 72)
    print(node.get("title", node["id"]))
    print("=" * 72)

    if retrieved["procedures"]:
        print("\nPROCEDURES")
        print("-" * 72)
        for item in retrieved["procedures"]:
            print(f"\n{item.get('title', item['id'])}")
            if item.get("description"):
                print(item["description"])

            steps = item.get("steps", [])
            for number, step in enumerate(steps, start=1):
                print(f"  {number}. {step}")

            for line in source_lines(item.get("source_ids", []), sources):
                print(line)

    if retrieved["laws"]:
        print("\nLAWS")
        print("-" * 72)
        for item in retrieved["laws"]:
            print(f"\n{item.get('title', item['id'])}")
            print(item.get("statement", ""))
            for line in source_lines(item.get("source_ids", []), sources):
                print(line)

    if retrieved["definitions"]:
        print("\nDEFINITIONS")
        print("-" * 72)
        for item in retrieved["definitions"]:
            print(f"\n{item.get('term', item['id'])}")
            print(item.get("definition", ""))
            for line in source_lines(item.get("source_ids", []), sources):
                print(line)

    if not any(retrieved.values()):
        print("\nNo legal knowledge is attached to this navigation node.")

    options = node.get("options", [])

    if options:
        print("\nOPTIONS")
        print("-" * 72)
        for number, option in enumerate(options, start=1):
            print(f"{number}. {option['label']}")

    return options


def choose_option(options):
    """Get a valid numbered terminal selection."""
    while True:
        raw = input("\nSelect an option (or q to quit): ").strip()

        if raw.lower() in {"q", "quit", "exit"}:
            return None

        try:
            choice = int(raw)
        except ValueError:
            print("Enter the number of an option.")
            continue

        if 1 <= choice <= len(options):
            return options[choice - 1]

        print(f"Enter a number from 1 to {len(options)}.")


def main():
    print("Loading Prototype 1 data...")

    pathway = load_json(PATHWAY_FILE)
    source_data = load_json(SOURCE_FILE)
    sources = build_index(source_data.get("sources", []), "source")
    knowledge = load_knowledge()

    nodes = validate_pathway(pathway, knowledge, sources)

    print(
        f"Loaded {len(nodes)} pathway nodes, "
        f"{len(knowledge['definitions'])} definitions, "
        f"{len(knowledge['laws'])} laws, "
        f"{len(knowledge['procedures'])} procedures, "
        f"and {len(sources)} sources."
    )
    print("Validation passed.")

    current_node_id = pathway["start_node"]

    while True:
        current_node = nodes[current_node_id]

        # Knowledge retrieval
        retrieved = retrieve_node_knowledge(current_node, knowledge)

        # Response compilation + terminal display
        options = print_response(current_node, retrieved, sources)

        if not options:
            print("\nThis node has no navigation options. Ending test.")
            break

        selected_option = choose_option(options)

        if selected_option is None:
            print("\nPrototype test ended.")
            break

        # Decision engine: Prototype 1 is simple deterministic traversal.
        current_node_id = selected_option["next_node"]


if __name__ == "__main__":
    main()
