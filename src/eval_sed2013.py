import math
import os.path
import random
import sys
import time

def nmi(doc2clust, doc2cat):        
    dict1 = {}
    for l in doc2clust:
        if not dict1.has_key(doc2clust[l][0]):
            dict1[doc2clust[l][0]] = set([])
        dict1[doc2clust[l][0]].add(int(l))
        
    dict2 = {}
    for l in doc2cat:
        if not dict2.has_key(doc2cat[l][0]):
            dict2[doc2cat[l][0]] = set([])
        dict2[doc2cat[l][0]].add(int(l))
        
    num = 0.0
    den_gs = 0.0
    den_my = 0.0
    
    for event_my in dict1:
        den_gs += (float(len(dict1[event_my])) / float(len(doc2cat))) * math.log(float(len(dict1[event_my])) / float(len(doc2cat)), 2)
        eset = set()
        for entry in dict1[event_my]:
            for en in doc2cat[str(entry)]:
                eset.add(str(en));
        for event_gs in eset:
            num += (float(len(dict1[event_my] & dict2[event_gs]))/float(len(doc2cat))) * (math.log((float(len(doc2cat)) * float(len(dict1[event_my] & dict2[event_gs]))) / (float(len(dict1[event_my])) * float(len(dict2[event_gs]))), 2))
                                                                            
    for event_gs in dict2:
        den_my += float(len(dict2[event_gs])) / float(len(doc2cat)) * math.log(float(len(dict2[event_gs])) / float(len(doc2cat)), 2)
        
    nmi = num / (((-1)*den_gs + (-1)*den_my) / 2)
    
    return nmi

def microf1(doc2clust, doc2cat):        
    dict1 = {}
    for l in doc2clust:
        if not dict1.has_key(doc2clust[l][0]):
            dict1[doc2clust[l][0]] = set([])
        dict1[doc2clust[l][0]].add(int(l.replace('_','')))
        
    dict2 = {}
    for l in doc2cat:
        if not dict2.has_key(doc2cat[l][0]):
            dict2[doc2cat[l][0]] = set([])
        dict2[doc2cat[l][0]].add(int(l.replace('_','')))
            
    prec = 0.0
    rec = 0.0
    
    for doc in doc2cat:
        prec += ( float(len(dict1[doc2clust[doc][0]] & dict2[doc2cat[doc][0]])) / float(len(dict1[doc2clust[doc][0]])) )
        rec  += ( float(len(dict1[doc2clust[doc][0]] & dict2[doc2cat[doc][0]])) / float(len(dict2[doc2cat[doc][0]])) )

    prec = prec / float(len(doc2cat))
    rec = rec / float(len(doc2cat))

    fmeasure = 2.0 * prec * rec / (prec + rec);
    #print prec, rec, fmeasure

    return fmeasure

def catf1(doc2clust, doc2cat):
    dict1 = {}
    for l in doc2clust:
        if not dict1.has_key(doc2clust[l][0]):
            dict1[doc2clust[l][0]] = set([])
        dict1[doc2clust[l][0]].add(int(l.replace('_','')))
        
    dict2 = {}
    for l in doc2cat:
        if not dict2.has_key(doc2cat[l][0]):
            dict2[doc2cat[l][0]] = set([])
        dict2[doc2cat[l][0]].add(int(l.replace('_','')))

    totPrec = 0.0
    totRec = 0.0
    totF1 = 0.0

    perCatPrec = {}
    perCatRec = {}
    perCatF1 = {}
    
    for category in dict2.keys():
        prec = ( float(len(dict1[category] & dict2[category])) / float(len(dict1[category])) )
        rec  = ( float(len(dict1[category] & dict2[category])) / float(len(dict2[category])) )
        fmeasure = 0
        if((prec + rec) != 0):
            fmeasure= 2.0 * prec * rec / (prec + rec);
        totPrec += prec
        totRec += rec
        totF1 += fmeasure
        perCatPrec[category] = prec
        perCatRec[category] = rec
        perCatF1[category] = fmeasure

    n_categories = float(len(dict2.keys()))
    avPrec = totPrec / n_categories
    avRec = totRec / n_categories
    avF1 = totF1 / n_categories

    return (perCatPrec, perCatRec, perCatF1, avPrec, avRec, avF1)


def divf1(doc2clust, doc2cat):        
    dict1 = {}
    for l in doc2clust:
        if not dict1.has_key(doc2clust[l][0]):
            dict1[doc2clust[l][0]] = set([])
        dict1[doc2clust[l][0]].add(int(l))
        
    dict2 = {}
    for l in doc2cat:
        if not dict2.has_key(doc2cat[l][0]):
            dict2[doc2cat[l][0]] = set([])
        dict2[doc2cat[l][0]].add(int(l))
    
    tp = 0.0
    fp = 0.0
    fn = 0.0
    
    for doc in doc2cat:       
        tp += float(len(dict1[doc2clust[doc][0]] & dict2[doc2cat[doc][0]])) - 1.0
        fp += float(len(dict1[doc2clust[doc][0]])) - float(len(dict1[doc2clust[doc][0]] & dict2[doc2cat[doc][0]]))
        fn += float(len(dict2[doc2cat[doc][0]])) - float(len(dict1[doc2clust[doc][0]] & dict2[doc2cat[doc][0]]))

    fmeasure = 2.0 * tp / ((2.0 * tp) + fn + fp);

    return fmeasure


def load_categories(category_file):
    """
    returns (doc -> cluster, set of all categories)
    """
    doc2cat= {}
    
    for line in category_file:
        tokens = line.strip().split(' ')
        docid = tokens[0]
        categories = tokens[1:]
        doc2cat[docid] = categories

    return doc2cat 

def assign_categories_to_clusters(doc2clust, doc2cat):
    clust2cat = {}
    clust2size = {} 
    
    for docid, doc_categories in doc2cat.iteritems():
        for cluster in doc2clust[docid]:
            if cluster in clust2cat:
                clust_cats = clust2cat[cluster]
            else:
                clust_cats = []
                clust2cat[cluster] = clust_cats 
            
            for category in doc_categories:
                clust_cats.append(category)

            if cluster in clust2size:
                clust2size[cluster] += 1
            else:
                clust2size[cluster] = 1

    return (clust2cat, clust2size)

def load_clusters(cluster_file):
    doc2clust = {} 

    for line in cluster_file:
        line = line.strip()
        if line[0] == '#':
            continue
        tokens = line.split(' ')
        docid = tokens[0]
        line_doc_clusters = tokens[1:]
        if docid in doc2clust:
            doc_clusters = doc2clust[docid]
        else:
            doc_clusters = []
            doc2clust[docid] = doc_clusters
        for cluster in line_doc_clusters:
            doc_clusters.append(cluster)

    return doc2clust

def unique_values(map): 
    unique = set()

    for key, list in map.iteritems():
        for item in list: 
            unique.add(item)

    return unique

def remove_docs_no_categories(doc2clust, doc2cat):
    """
    returns the number of documents with no categories
    """
    remove = []

    for docid, categories in doc2cat.iteritems():
        if len(categories) == 0:
            remove.append(docid)

    for docid in doc2clust.keys():
        if docid not in doc2cat:
            remove.append(docid)

    for docid in remove:
        if docid in doc2clust:
            del doc2clust[docid]
        if docid in doc2cat:
            del doc2cat[docid]

    return len(remove)

def print_clust_size_distribution(clust2size, log):
    # cluster size distribution 
    sizes = clust2size.values()
    mean_size = float(sum(sizes)) / len(sizes)
    diff_mean_squared = [(float(x) - mean_size)**2 for x in sizes]
    observations = len(sizes) # population distribution (i.e. all clusters) no need to subtract 1 
    stddev = math.sqrt(sum(diff_mean_squared) / float(observations))
    log(' - Cluster Sizes:')
    log('   * Mean = %f' % mean_size)
    log('   * Standard Deviation = %f' % stddev)
    log('   * max = %d' % max(sizes))
    log('   * min = %d' % min(sizes))

def load(cluster_file, category_file, log, options):
    log('> CLUSTER STATISTICS\n')
    log('Original Cluster Statistics:')
    doc2clust = load_clusters(cluster_file)
    original_document_count = len(doc2clust)
    log(' - Document Count = %s' % original_document_count) 

    all_clusters = unique_values(doc2clust)
    original_cluster_count = len(all_clusters)
    log(' - Cluster Count = %s' % original_cluster_count)

    doc2cat = load_categories(category_file)
    all_categories = unique_values(doc2cat)
    log(' - Category Count = %s' % len(all_categories))

    original_clust2size = {}
    for cluster, docs in invert(doc2clust).items():
        original_clust2size[cluster] = len(docs)
    print_clust_size_distribution(original_clust2size, log)
    log('')

    # check the docid match between categories and clusters
    category_docs = set(doc2cat.keys())
    cluster_docs = set(doc2clust.keys())
    missing = category_docs.difference(cluster_docs) 
    if missing:
        raise Exception('Document IDs do not match between cluster and category file. missing = %s' % missing)
    
    # remove docs with no category information
    no_cat_count = remove_docs_no_categories(doc2clust, doc2cat)
    clust2cat, clust2size = assign_categories_to_clusters(doc2clust, doc2cat) # reassign categories to clusters now that some categories have been removed

    if no_cat_count > 0:
        log('Reduced Cluster Statistics:')
        log(' - Documents with no categories = %s' % no_cat_count)
        log(' - Reduced Document Count = %s' % len(doc2clust))
        log(' - Reduced Cluster Count = %s' % len(unique_values(doc2clust)))
        print_clust_size_distribution(clust2size, log)
        log('')

    return (doc2clust, doc2cat, clust2cat, clust2size, original_cluster_count, original_document_count, all_categories)


def invert(key2values): # key -> list of values
    value2keys = {}
    for key, values in key2values.items():
        for value in values:
            if value in value2keys:
                value2keys[value].append(key)
            else:
                value2keys[value] = [key]
    return value2keys

def generate_baseline(doc2clust, clust2size, category_file, doc2cat, log):
    """
    This only works for single label categories and submissions.
    """
    docids = doc2clust.keys()
    # shuffle document IDs based on hash of category_file so that the same random baseline is always generated with the same inputs
    category_file.seek(0)
    random.seed(category_file.read()) # seed random number generator with hash of category file contents     
    random.shuffle(docids)
    baseline_doc2clust = {}
    offset = 0
    for cluster, size in clust2size.items():
        # choose next cluster from randomly shuffled document list that matches the cluster size
        cluster_docids = docids[offset:offset+size]
        for docid in cluster_docids:
            baseline_doc2clust[docid] = [cluster]
        offset += size
    baseline_clust2cat, baseline_clust2size = assign_categories_to_clusters(baseline_doc2clust, doc2cat)
    log('Baseline Cluster Statistics:')
    print_clust_size_distribution(baseline_clust2size, log)
    log('')
    return baseline_doc2clust, baseline_clust2cat, baseline_clust2size

def changeDoc2EventNoEvent(doc2TypeMap):
    new_doc2TypeMap = {}
    for doc in doc2TypeMap.keys():
        if(doc2TypeMap[doc]==['no_event']):
            new_doc2TypeMap[doc]=['no_event']
        else:
            new_doc2TypeMap[doc]=['event']
    return new_doc2TypeMap	

def run(cluster_file, category_file, log, options):
    """
    returns (list of (metric, score), cluster_count, document_count)
    """
    log('----------------------------------------------------------------------------')
    log('SED 2013 - Evaluation Tool')
    log('----------------------------------------------------------------------------')

    start = time.time()
    doc2clust, doc2cat, clust2cat, clust2size, cluster_count, document_count, categories = load(cluster_file, category_file, log, options)
    stop = time.time()
    log('Reading in the CSV files took %.2f seconds' % (stop - start))
    log('')

    if '--baseline' in options:
        start = time.time()
        baseline_doc2clust, baseline_clust2cat, baseline_clust2size = generate_baseline(doc2clust, clust2size, category_file, doc2cat, log)
        stop = time.time()
        log('Generating the baseline took %.2f seconds' % (stop - start))
        log('')

    scores = []
    
    def append_score(score):
        scores.append(score)
        log(' | %-36s | %.4f   |' % scores[-1]) 
        
    def append_long_score(score):
        scores.append(score)
        log(' | %-50s | %.4f   |' % scores[-1]) 
        
    if '--challenge1' in options:
        start = time.time()
        print '> RESULTS FOR CHALLENGE 1'
        print ' ---------------------------------------------------'
        score_tf1 = microf1(doc2clust, doc2cat)
        append_score(('F1 (Main Score)', score_tf1))
        print ' |-------------------------------------------------|'
        score_nmi = nmi(doc2clust, doc2cat)
        append_score(('NMI', score_nmi))
        score_cf1 = divf1(doc2clust, doc2cat)
        append_score(('F1 (Div)', score_cf1))
        if '--baseline' in options:
            baseline_score = microf1(baseline_doc2clust, doc2cat)
            append_score(('Random Baseline F1', baseline_score))
            append_score(('Divergence F1', score_tf1 - baseline_score))
        print ' ---------------------------------------------------'
        stop = time.time()
        log('')
        log('Calculation of Scores took %.2f seconds' % (stop - start))
        
    if '--challenge2' in options:
        start = time.time()
        print '> RESULTS FOR CHALLENGE 2'
        print ' ---------------------------------------------------'
        print ' PER EVENT TYPE RESULTS'
        print ' ---------------------------------------------------'
        #score_tf1 = microf1(doc2clust, doc2cat)
        perCatPrec,perCatRec,perCatF1,perCatAvPrec,perCatAvRec,perCatAvF1 = catf1(doc2clust, doc2cat)
        for category in perCatF1.keys():
            append_score(('F1 for category >' + category + '<', perCatF1[category]))
        print ' |-------------------------------------------------|'
        append_score(('F1 per category, average ', perCatAvF1))

        if '--baseline' in options:
            print ' |-------------------------------------------------|'

            baseline_prec,baseline_rec,baseline_f1,baselineAvPrec,baselineCatAvRec,baselineCatAvF1 = catf1(baseline_doc2clust, doc2cat)
            for category in baseline_f1.keys():
                #append_score(('Random Baseline F1: ' + category, baseline_f1[category]))
                append_score(('Divergence F1 >' + category + '<', perCatF1[category] - baseline_f1[category]))
            #append_score(('Random Baseline F1 per category, avg ', baselineCatAvF1))
            append_score(('Divergence F1 per category, average', perCatAvF1 - baselineCatAvF1))
        print ' ---------------------------------------------------'

        print '\n EVENT/NO EVENT RESULTS'
        print ' -----------------------------------------------------------------'
        eneDoc2Clust=changeDoc2EventNoEvent(doc2clust)
        eneDoc2Cat=changeDoc2EventNoEvent(doc2cat)
#        score_tf1 = microf1(doc2clust, doc2cat)
        enePerCatPrec,enePerCatRec,enePerCatF1,enePerCatAvPrec,enePerCatAvRec,enePerCatAvF1 = catf1(eneDoc2Clust, eneDoc2Cat)
        for category in enePerCatF1.keys():
            append_long_score(('Event/No event F1 for category >' + category + '<', enePerCatF1[category]))
        append_long_score(('Event/No event F1 per category, average ', enePerCatAvF1))

        if '--baseline' in options:
            print ' |---------------------------------------------------------------|'
            eneBaseDoc2Clust=changeDoc2EventNoEvent(baseline_doc2clust)
            ene_baseline_prec,ene_baseline_rec,ene_baseline_f1,ene_baseline_AvPrec,ene_baseline_AvRec,ene_baseline_AvF1 = catf1(eneBaseDoc2Clust, eneDoc2Cat)
            for category in ene_baseline_f1.keys():
                #append_long_score(('Event/No event Random Baseline F1 for category '+category, ene_baseline_f1[category]))
                append_long_score(('Event/No event Divergence F1 for category ' + category, enePerCatF1[category] - ene_baseline_f1[category]))
            #append_score(('Event/No event Random Baseline F1, average '+category, ene_baseline_AvF1))
            append_long_score(('Event/No event Divergence F1, average ' + category, enePerCatAvF1 - ene_baseline_AvF1))

        print ' -----------------------------------------------------------------'
        stop = time.time()
        log('')
        log('Calculation of Scores took %.2f seconds' % (stop - start))

    return (scores, cluster_count, document_count)

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print("""ABOUT
----------
This program compares a clustering to a ground truth set of categories according to multiple different measures.

For details of the MediaEval SED evaluation see http://www.multimediaeval.org/mediaeval2013/sed2013/.

CREDITS
-----------
The following people have contributed code to this evaluation tool
Timo Reuter <treuter@cit-ec.uni-bielefeld.de>
Chris de Vries <chris@de-vries.id.au>
Georgios Petkos <gpetkos@iti.gr>

If you have any questions, please contact Timo Reuter (treuter@cit-ec.uni-bielefeld.de).

USAGE
----------
%s [zero or more OPTIONS] [in: ground truth] [in: own submission]

OPTIONS (default = -nmi):
    --baseline = Report measures with Divergence from Random Baseline
                 (To be used with Challenge 1 and 2)
    --challenge1 = Calculate measurement relevant for Challenge 1
    --challenge2 = Calculate measurement relevant for Challenge 2
    --stats = print detailed statistics of categories in clusters"""
        % sys.argv[0])
        sys.exit(1)

    def log(s):
        print(s)

    options_list = sys.argv[1:-2]
    if not options_list:
        options = set(['--challenge1'])
    else:
        options = set([x.lower() for x in options_list])

    category_file_path = sys.argv[-2]
    category_file = open(category_file_path, 'r')

    cluster_file_path = sys.argv[-1]
    cluster_file = open(cluster_file_path, 'r')
    try:
        start = time.time()
        scores, cluster_count, document_count = run(cluster_file, category_file, log, options)
        stop = time.time()
        log('Total Processing Time: %.2f seconds' % (stop - start))
        log('----------------------------------------------------------------------------')
    finally:
        cluster_file.close()
        category_file.close()

