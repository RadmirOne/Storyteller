"""Dependency-free editorial/branch audit; complements, not replaces, Kotlin engine tests."""
import json
from collections import Counter
from pathlib import Path

root = Path(__file__).resolve().parents[1]
story = json.loads((root / 'shared/src/commonMain/composeResources/files/story.json').read_text())
seen, endings, metrics = set(), Counter(), []
combinations = {}

def walk(scene, key, variables, trail=(), words=0, first_choices=()):
    location = (scene, key)
    assert location not in trail, f'Cycle: {location}'
    node = story['scenes'][scene]['nodes'][key]
    assert node['id'] == key
    assert len(node['text']) <= 300, location
    seen.add(location)
    words += len(node['text'].split())
    trail += (location,)
    if node.get('choices'):
        assert 'nextNodeId' not in node, location
        available = []
        for choice in node['choices']:
            referenced = set(choice.get('conditions', {})) | set(choice.get('effects', {}).get('set', {})) | set(choice.get('effects', {}).get('add', {}))
            assert referenced <= story['initialVariables'].keys()
            if all(variables[k] >= value for k, value in choice.get('conditions', {}).items()):
                available.append(choice)
            else:
                assert choice.get('unavailableReason'), (location, choice['id'])
        assert available, f'Dead end: {location}'
        for choice in available:
            values = dict(variables)
            values.update(choice.get('effects', {}).get('set', {}))
            for k, value in choice.get('effects', {}).get('add', {}).items():
                values[k] += value
            early = first_choices + (choice['id'],) if scene in ('arrival', 'preparation', 'opening') else first_choices
            walk(choice.get('targetSceneId', scene), choice['targetNodeId'], values, trail,
                 words + len(choice['text'].split()), early)
    elif node.get('nextNodeId'):
        walk(node.get('nextSceneId', scene), node['nextNodeId'], variables, trail, words, first_choices)
    else:
        endings[location] += 1
        metrics.append((words, len(trail)))
        combinations.setdefault(first_choices, set()).add(location)

walk(story['startSceneId'], story['startNodeId'], story['initialVariables'])
all_nodes = {(scene, key) for scene, data in story['scenes'].items() for key in data['nodes']}
assert seen == all_nodes, f'Unreachable: {all_nodes - seen}'
expected = {('ilya_evening', 'promise'), ('ilya_evening', 'friend'), ('mark_evening', 'promise'), ('mark_evening', 'friend'), ('friends', 'home')}
assert set(endings) == expected
assert len(combinations) == 8
assert all(value == expected for value in combinations.values()), combinations
assert min(words for words, _ in metrics) >= 2400, 'Demo is too short on one route'
print(f'{len(seen)} pages; {sum(endings.values())} routes; {len(endings)} endings; all 8 early-choice combinations retain every ending')
print(f'Words per route: {min(w for w, _ in metrics)}–{max(w for w, _ in metrics)}; pages: {min(p for _, p in metrics)}–{max(p for _, p in metrics)}')
for ending, count in sorted(endings.items()):
    print('/'.join(ending), count)
