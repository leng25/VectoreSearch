
import os
import mwxml
import bz2
from mwxml import Dump, Page
import re

def clean_text(wikitext):
    # Remove XML/HTML tags
    wikitext = re.sub(r'</?[^>]+>', '', wikitext)
    # Remove template calls (e.g., {{...}})
    wikitext = re.sub(r'\{\{.*?\}\}', '', wikitext, flags=re.DOTALL)
    # Remove file/image links (e.g., [[File:...]] or [[Image:...]])
    wikitext = re.sub(r'\[\[(File|Image):.*?\]\]', '', wikitext, flags=re.IGNORECASE)
    # Remove external links (e.g., [http://...])
    wikitext = re.sub(r' \[https?://.*?]', '', wikitext)
    # Remove internal links (e.g., [[...]]) and keep the link text
    wikitext = re.sub(r'\[\[(?:[^|]|\])*?(\|[^|\]]*?)?\]\]', r'\1', wikitext)
    # Remove '''bold''' and ''italic'' markup
    wikitext = re.sub(r"'''(.*?)'''", r'\1', wikitext)
    wikitext = re.sub(r"''(. *?)''", r'\1', wikitext)
    # Remove headings (e.g., ==...==)
    wikitext = re.sub(r'==.*?==', '', wikitext)
    # Remove list markers (*, #)
    wikitext = re.sub(r'^\s*[\*#]\s*', '', wikitext, flags=re.MULTILINE)
    # Remove indentation markers (:)
    wikitext = re.sub(r'^\s*:\s*', '', wikitext, flags=re.MULTILINE)
    # Remove horizontal rules (----)
    wikitext = re.sub(r'----', '', wikitext)
    # Replace multiple newlines with a single newline
    wikitext = re.sub(r'\n{2,}', '\n', wikitext)
    return wikitext.strip()

def main():
    dump_path = 'src/main/resources/wiki/simplewiki-latest-pages-articles.xml.bz2'
    output_dir = 'src/main/resources/documents'

    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    dump = Dump.from_file(bz2.open(dump_path))

    for page in dump:
        if page.namespace == 0 and not page.redirect:
            for revision in page:
                try:
                    title = page.title.replace('/', '_') # Sanitize title for filename
                    filename = os.path.join(output_dir, f"{title}.txt")
                    if not os.path.exists(filename):
                        print(f"Extracting: {page.title}")
                        text = clean_text(revision.text)
                        with open(filename, 'w', encoding='utf-8') as f:
                            f.write(text)
                except Exception as e:
                    print(f"Error processing page {page.title}: {e}")

if __name__ == '__main__':
    main()
