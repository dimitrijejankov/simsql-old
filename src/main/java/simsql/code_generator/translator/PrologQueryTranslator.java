package simsql.code_generator.translator;

/**
 * Translates a query into an object of type E.
 */
interface PrologQueryTranslator<E> {

    E translate(PrologQuery q);
}
